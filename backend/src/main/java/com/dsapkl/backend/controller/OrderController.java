package com.dsapkl.backend.controller;

import com.dsapkl.backend.dto.CartForm;
import com.dsapkl.backend.dto.CartOrderDto;
import com.dsapkl.backend.dto.CheckoutRequest;
import com.dsapkl.backend.repository.OrderDto;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.Order;
import com.dsapkl.backend.entity.OrderStatus;
import com.dsapkl.backend.exception.NotEnoughStockException;
import com.dsapkl.backend.service.OrderService;
import com.dsapkl.backend.service.UserActivityLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserActivityLogService logService;

    /**
     * 단일 상품 바로 주문
     */
    @PostMapping("/order")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> order(@RequestBody CartForm cartForm, HttpServletRequest request) {

        //CartController 에 작성해둔 세션 정보 조회하는 기능 공용으로 사용
        Member member = CartController.getMember(request);

        if (member == null) {
            return new ResponseEntity<>(Map.of("status", "fail", "message", "Login required for this service."), HttpStatus.UNAUTHORIZED);
        }
        Long orderId;
        try {
            // 주문 저장 후 저장된 orderId 반환
            orderId = orderService.order(member.getId(), cartForm.getItemId(), cartForm.getCount(), cartForm.getPaymentIntentId());
        } catch (NotEnoughStockException e) {
            return new ResponseEntity<>(Map.of("status", "fail", "message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("orderId", orderId);

//        System.out.println(response);

        return ResponseEntity.ok(response);
    }

    /**
     * 주문 내역 조회
     */
    @GetMapping("/orders")
    public String findOrder(OrderStatus status, Model model, HttpServletRequest request) {
        List<OrderDto> findOrders = getOrderDetails(null, status, model, request);
        model.addAttribute("orderDetails", findOrders);
        return "order/orderList";
    }

    @GetMapping("/orders/success")
    public String findOrderSuccess(@RequestParam(required = false) String sessionId, OrderStatus status, Model model, HttpServletRequest request) throws JsonProcessingException {
        List<OrderDto> findOrders = getOrderDetails(sessionId, status, model, request);
        model.addAttribute("orderDetails", findOrders);
        return "redirect:/orders";
    }

    private List<OrderDto> getOrderDetails(String sessionId, OrderStatus status, Model model, HttpServletRequest request) {
        Stripe.apiKey = "sk_test_51QclmbPPwZvRdRPfWv7wXxklQBavqLzNsxg3hsnaErdkjaZSvWCncfJXaQ9yUbvxCaUPRfEMsp2GXGwvSd2QHcHn00XH6z4sld";
        Member member = CartController.getMember(request);
        List<OrderDto> findOrders = Collections.emptyList();
        if (sessionId != null) {
            try {
                Session session = Session.retrieve(sessionId);
                String jsessionId = request.getSession().getId();
                String orderInfoJson = session.getMetadata().get("orderInfo");
                ObjectMapper objectMapper = new ObjectMapper();

                String paymentIntentId = session.getPaymentIntent();
                CheckoutRequest checkoutRequest = objectMapper.readValue(orderInfoJson, CheckoutRequest.class);
                model.addAttribute("checkoutRequest", checkoutRequest);

                CartForm cartForm = new CartForm(checkoutRequest.getItemId(), checkoutRequest.getCount(), paymentIntentId);
                model.addAttribute("cartForm", cartForm);

                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("Cookie", "JSESSIONID=" + jsessionId);

                HttpEntity<CartForm> requestEntity = new HttpEntity<>(cartForm, headers);
                ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:8888/order", requestEntity, Map.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    Map<String, Object> responseBody = response.getBody();

                    if ("success".equals(responseBody.get("status"))) {
                        System.out.println("Buy Now Success");  // 콘솔에 메시지 출력
                        findOrders = orderService.findOrdersDetail(member.getId(), status);
                    } else {
                        System.out.println("Buy Now Failed");
                    }
                } else {
                    System.out.println("Server error: " + response.getStatusCode());
                }

            } catch (StripeException | JsonProcessingException e) {
                e.printStackTrace();
                model.addAttribute("error", "Failed to retrieve payment information.");
            }
        }   else {
            findOrders = orderService.findOrdersDetail(member.getId(), status);
        }
        return findOrders;
    }

    /**
     * 장바구니 상품 주문
     */
    @PostMapping("/orders")
    @ResponseBody
    public ResponseEntity<String> orders(@RequestBody CartOrderDto cartOrderDto, HttpServletRequest request, String paymentIntentId) {
        if (cartOrderDto.getCartOrderDtoList().isEmpty()) {
            return new ResponseEntity<>("Please select at least one product to order.", HttpStatus.FORBIDDEN);
        }

        Member member = CartController.getMember(request);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login required for this service.");
        }

        try {
            orderService.orders(member.getId(), cartOrderDto);
            // 주문 성공 후 로그 남기기
            logService.logEvent(member.getId(), "ORDER_COMPLETE", 
                String.format("Order completed with %d items", cartOrderDto.getCartOrderDtoList().size()));
            return ResponseEntity.ok("Cart Order Success");
        } catch (NotEnoughStockException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 주문 취소
     */
    @PostMapping("/order/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
        try {
            // 주문 취소 실행 (Order 반환)
            Order order = orderService.cancelOrder(orderId);
            
            // 주문 취소 로그 남기기
            logService.logEvent(order.getMember().getId(), "ORDER_CANCEL", 
                String.format("Order %d cancelled", orderId));
            
            return ResponseEntity.ok("Order cancelled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
