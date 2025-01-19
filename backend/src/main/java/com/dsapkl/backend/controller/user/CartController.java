package com.dsapkl.backend.controller.user;

import com.dsapkl.backend.config.AuthenticatedUser;
import com.dsapkl.backend.dto.CartForm;
import com.dsapkl.backend.dto.CartItemForm;
import com.dsapkl.backend.dto.CartOrderDto;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.OrderService;
import com.dsapkl.backend.util.SessionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;

    /**
     *  장바구니 조회
     */

    @GetMapping("/cart")
    public String cartView(Model model, @AuthenticationPrincipal AuthenticatedUser user) throws JsonProcessingException {
        List<CartQueryDto> cartViews = cartViewDetails(null, model, user);
        model.addAttribute("cartItemListForm", cartViews);
        return "cart/cartView";
    }

    @GetMapping("/cart/success")
    public String cartSuccessView(@RequestParam(required = false) String sessionId, Model model, @AuthenticationPrincipal AuthenticatedUser user) throws JsonProcessingException {
        List<CartQueryDto> cartViews = cartViewDetails(sessionId, model, user);
        model.addAttribute("cartItemListForm", cartViews);
        return "redirect:../cart";
    }

    private List<CartQueryDto> cartViewDetails(@RequestParam(required = false) String sessionId, Model model, AuthenticatedUser user)
    throws JsonProcessingException {
        Stripe.apiKey = "sk_test_51QclmbPPwZvRdRPfWv7wXxklQBavqLzNsxg3hsnaErdkjaZSvWCncfJXaQ9yUbvxCaUPRfEMsp2GXGwvSd2QHcHn00XH6z4sld";

        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        List<CartQueryDto> cartItemListForm = Collections.emptyList();

        if (sessionId != null) {
            try{
                Session session = Session.retrieve(sessionId);
                String orderInfoJson = session.getMetadata().get("orderInfo");
                ObjectMapper objectMapper = new ObjectMapper();

                String paymentIntentId = session.getPaymentIntent();


                List<CartQueryDto> cartOrderList = objectMapper.readValue(orderInfoJson, new TypeReference<List<CartQueryDto>>() {});
                model.addAttribute("cartOrderList", cartOrderList);

                List<CartForm> cartFormList = cartOrderList.stream()
                        .map(cartQueryDto -> new CartForm(cartQueryDto.getCartItemId(), cartQueryDto.getItemId(), cartQueryDto.getCount(), paymentIntentId))
                        .collect(Collectors.toList());

                CartOrderDto cartOrderDto = new CartOrderDto();
                cartOrderDto.setCartOrderDtoList(cartFormList);

                // SecurityContextHolder 에서 현재 인증 정보 가져오기
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String jsessionId = ((WebAuthenticationDetails)authentication.getDetails()).getSessionId();

                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("Cookie", "JSESSIONID=" + jsessionId);

                HttpEntity<CartOrderDto> requestEntity = new HttpEntity<>(cartOrderDto, headers);
                ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8888/user/orders", requestEntity, String.class);
                log.debug("Order response: {}", response.getBody());
                cartItemListForm = cartService.findCartItems(user.getId());

            } catch (StripeException e) {
                e.printStackTrace();
                model.addAttribute("error", "결제 정보를 불러오는 데 실패했습니다.");
            }
        } else {
            cartItemListForm = cartService.findCartItems(user.getId());
        }

        return cartItemListForm;
    }

}
