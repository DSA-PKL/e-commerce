package com.dsapkl.backend.restcontroller.user;

import com.dsapkl.backend.config.AuthenticatedUser;
import com.dsapkl.backend.dto.DataDto;
import com.dsapkl.backend.dto.CheckoutRequest;
import com.dsapkl.backend.exception.UnauthorizedException;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/api")
public class CheckoutController {

    @Value("${stripe.secret.key}")
    private String secretKey;

    @PostMapping("/checkout/create-checkout-session-multi")
    public Map<String, String> createCheckoutSession(@RequestBody DataDto request) throws StripeException, JsonProcessingException {

        List<CartQueryDto> cartOrderList = request.getCartQueryDto();

        long totalAmount = cartOrderList.stream()
                                        .mapToLong(item -> item.getCount() * item.getPrice())
                                        .sum();

        // Stripe 비밀키 설정
        Stripe.apiKey = secretKey;


        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        for(CartQueryDto item : cartOrderList) {
            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem.builder()
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("krw") // 통화 설정
                                            .setUnitAmount((long)item.getPrice()) // 금액 설정 (단위: won)
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(item.getItemName()) // 상품명
                                                            .build()
                                            )
                                            .build()
                            )
                            .setQuantity((long)item.getCount())
                            .build();

            lineItems.add(lineItem);
        }

        // Checkout 세션 생성
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .addAllLineItem(lineItems)
                        .setMode(SessionCreateParams.Mode.PAYMENT) // 결제 모드
                        .putMetadata("orderInfo", new ObjectMapper().writeValueAsString(cartOrderList))
                        .setSuccessUrl("http://localhost:8888/cart/success?sessionId={CHECKOUT_SESSION_ID}") // 성공 시 리다이렉트 URL
                        .setCancelUrl("http://localhost:8888/members") // 취소 시 리다이렉트 URL
                        .build();

        Session session = Session.create(params);

        // 클라이언트에 세션 ID 반환
        Map<String, String> responseData = new HashMap<>();
        responseData.put("sessionId", session.getId());
        return responseData;
    }

    @PostMapping("/checkout/create-checkout-session-single")
    public Map<String, String> createCheckoutSession(@RequestBody CheckoutRequest request,
                                                     @AuthenticationPrincipal AuthenticatedUser user) throws StripeException, JsonProcessingException {

        if (user == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        // Stripe 비밀키 설정
        Stripe.apiKey = secretKey;

        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("krw") // 통화 설정
                                        .setUnitAmount((long)request.getPrice()) // 금액 설정 (단위: won)
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName(request.getItemName()) // 상품명
                                                        .build()
                                        )
                                        .build()
                        )
                        .setQuantity((long)request.getCount())
                        .build();

        // Checkout 세션 생성
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .addLineItem(lineItem)
                        .setMode(SessionCreateParams.Mode.PAYMENT) // 결제 모드
                        .putMetadata("orderInfo", new ObjectMapper().writeValueAsString(request))
                        .setSuccessUrl("http://localhost:8888/user/orders/success?sessionId={CHECKOUT_SESSION_ID}") // 성공 시 리다이렉트 URL
                        .setCancelUrl("http://localhost:8888/members") // 취소 시 리다이렉트 URL
                        .build();

        Session session = Session.create(params);

        // 클라이언트에 세션 ID 반환
        Map<String, String> responseData = new HashMap<>();
        responseData.put("sessionId", session.getId());
        return responseData;
    }


    // 환불 처리
    @PostMapping("/refund")
    public Map<String, String> createRefund(@RequestParam String paymentIntentId) {
        Stripe.apiKey = secretKey;

        Map<String, String> response = new HashMap<>();
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();

            Refund refund = Refund.create(params);
            response.put("status", "success");
            response.put("refundId", refund.getId());
        } catch (StripeException e) {
            response.put("status", "failed");
            response.put("error", e.getMessage());
        }
        return response;
    }


}
