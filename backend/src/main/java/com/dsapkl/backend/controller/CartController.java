package com.dsapkl.backend.controller;

import com.dsapkl.backend.controller.dto.CartForm;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
//@Slf4j
public class CartController {

    private final CartService cartService;
//    private final ItemImageService itemImageService;

    /**
     *  장바구니 조회
     */
    @GetMapping("/cart")
    public String cartView(Model model, HttpServletRequest request) {

        Member member = getMember(request);

        if (member == null) {
            return "redirect:/members";
        }

        List<CartQueryDto> cartItemListForm = cartService.findCartItems(member.getId());

        model.addAttribute("cartItemListForm", cartItemListForm);

        return "cart/cartView";
    }

    /**
     *  장바구니 담기
     */
    @PostMapping("/cart")
    @ResponseBody
    public ResponseEntity<String> addCart(@ModelAttribute CartForm cartForm, HttpServletRequest request) {

        Member member = getMember(request);
        //비로그인 회원은 장바구니를 가질 수 없다.
        if (member == null) {
            return new ResponseEntity<String>("로그인이 필요합니다.", HttpStatus.BAD_REQUEST);
        }

        cartService.addCart(member.getId(), cartForm.getItemId(), cartForm.getCount());
        return ResponseEntity.ok("success");
    }



    //다른 컨트롤러에서도 사용하기 위해 static
    public static Member getMember(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        //비로그인 사용자
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return null;
        }

        //세션에 저장되어있는 회원정보 가져오기
        Member member = (Member) session.getAttribute(SessionConst.LOGIN_MEMBER);
        return member;
    }
}
