package com.dsapkl.backend.restcontroller.user;

import com.dsapkl.backend.config.AuthenticatedUser;
import com.dsapkl.backend.dto.CartForm;
import com.dsapkl.backend.dto.CartOrderDto;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.exception.NotEnoughStockException;
import com.dsapkl.backend.service.OrderService;
import com.dsapkl.backend.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderService orderService;

    /**
     * 단일 상품 바로 주문
     */
    @PostMapping("/order")
    public ResponseEntity<Map<String, Object>> order(@RequestBody CartForm cartForm, @AuthenticationPrincipal AuthenticatedUser user) {

        if (user == null) {
            return new ResponseEntity<>(Map.of("status", "fail", "message", "로그인이 필요한 서비스입니다."), HttpStatus.UNAUTHORIZED);
        }
        Long orderId;
        try {
            // 주문 저장 후 저장된 orderId 반환
            orderId = orderService.order(user.getId(), cartForm.getItemId(), cartForm.getCount(), cartForm.getPaymentIntentId());
        } catch (NotEnoughStockException e) {
            return new ResponseEntity<>(Map.of("status", "fail", "message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("orderId", orderId);

        return ResponseEntity.ok(response);
    }

    /**
     * 장바구니 상품 주문
     */
    @PostMapping("/orders")
    public ResponseEntity<String> orders(@RequestBody CartOrderDto cartOrderDto, HttpServletRequest request, String paymentIntentId) {

        //장바구니에서 아무 상품도 체크하지 않을 경우
        if (cartOrderDto.getCartOrderDtoList().isEmpty()) {
            return new ResponseEntity<>("하나 이상의 상품을 주문하셔야 합니다.", HttpStatus.FORBIDDEN);
        }

        //CartController 에 작성해둔 세션 정보 조회하는 기능 공용으로 사용
        Member member = SessionUtil.getMember(request);
        if (member == null) {
            return new ResponseEntity<>("로그인이 필요한 서비스입니다.", HttpStatus.UNAUTHORIZED);
        }

        try {
            orderService.orders(member.getId(), cartOrderDto);
        } catch (NotEnoughStockException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok("Cart Order Success");
    }

    /**
     * 주문 취소
     */
    @PostMapping("/order/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok("Success");
    }

}
