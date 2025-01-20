package com.dsapkl.backend.restcontroller.user;

import com.dsapkl.backend.config.AuthenticatedUser;
import com.dsapkl.backend.dto.CartForm;
import com.dsapkl.backend.dto.CartItemDto;
import com.dsapkl.backend.dto.CartItemForm;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class CartRestController {

    private final CartService cartService;

    /**
     *  장바구니 담기
     */
    @PostMapping("/cart")
    public ResponseEntity<Map<String, String>> addCart(@ModelAttribute CartItemDto cartItemDto, @AuthenticationPrincipal AuthenticatedUser user) {

//        Member member = SessionUtil.getMember(request);
        //비로그인 회원은 장바구니를 가질 수 없다.
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }

        cartService.addCart(cartItemDto, user.getEmail());

        return ResponseEntity.ok(Map.of("message", "Item added to your cart!", "redirectUrl", "/user/items/" + cartItemDto.getItemId()));
    }


    @DeleteMapping("/cart")
    public ResponseEntity<String> deleteCartItem(@RequestBody CartItemForm form) {

//        log.info("itemId={}", form.getCartItemId());

        if (cartService.findCartItem(form.getCartItemId()) == null) {
            return new ResponseEntity<String>("다시 시도해주세요.", HttpStatus.NOT_FOUND);
        }

        cartService.deleteCartItem(form.getCartItemId());

        return ResponseEntity.ok("success");
    }

}
