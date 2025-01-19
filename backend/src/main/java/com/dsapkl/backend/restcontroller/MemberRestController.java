package com.dsapkl.backend.restcontroller;

import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/members/api")
@RequiredArgsConstructor
public class MemberRestController {

    private final MemberService memberService;
    private final CartService cartService;

    @GetMapping("/members/check-email")
    public Map<String, Boolean> checkEmailDuplicate(@RequestParam("email") String email) {
        boolean isAvailable = memberService.isEmailAvailable(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isAvailable", isAvailable);
        return response;
    }
    @GetMapping("/members/check-phone")
    public Map<String, Boolean> checkPhoneDuplicate(@RequestParam("phoneNumber") String phoneNumber) {
        boolean isAvailable = memberService.isPhoneAvailable(phoneNumber);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isAvailable", isAvailable);
        return response;
    }
}
