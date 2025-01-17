package com.dsapkl.backend.controller;

import com.dsapkl.backend.controller.dto.FindEmailRequestDto;
import com.dsapkl.backend.controller.dto.FindPasswordRequestDto;
import com.dsapkl.backend.controller.dto.LoginForm;
import com.dsapkl.backend.controller.dto.MemberForm;
import com.dsapkl.backend.entity.Address;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final CartService cartService;

    /*
    회원가입
     */
    @GetMapping("/members/new")
    public String createMemberForm(@ModelAttribute("memberForm") MemberForm memberForm, Model model) {
        List<RoleCode> roleCodes = new ArrayList<>();
        roleCodes.add(new RoleCode("admin","판매자"));
        roleCodes.add(new RoleCode("user","구매자"));
        model.addAttribute("roleCodes", roleCodes);

        return "members/createMemberForm";
    }

    @Data
    @AllArgsConstructor
    static class RoleCode {
        private String code;
        private String displayName;
    }

    //회원가입
    @PostMapping("/members/new")
    public String createMember(@Valid @ModelAttribute MemberForm memberForm,
                               BindingResult bindingResult, Model model,
                               @RequestParam("role") String role) {

        //memberForm 객체에 binding 했을 때 에러
        if(bindingResult.hasErrors()) {
            List<RoleCode> roleCodes = new ArrayList<>();
            roleCodes.add(new RoleCode("admin", "판매자"));
            roleCodes.add(new RoleCode("user", "구매자"));
            model.addAttribute("roleCodes", roleCodes);
            return "members/createMemberForm";
        }

        Address address = new Address(
                memberForm.getCity(),
                memberForm.getStreet(),
                memberForm.getZipcode());

        try {
            Member member = Member.builder()
                    .name(memberForm.getName())
                    .email(memberForm.getEmail())
                    .password(memberForm.getPassword())
                    .birthDate(memberForm.getBirthDate())
                    .phoneNumber(memberForm.getPhoneNumber())
                    .address(address)
                    .build();

            member.changeRole(role);
            memberService.join(member);
        } catch (IllegalStateException e){
            model.addAttribute("errorMessage", e.getMessage());
            return "members/createMemberForm";
        }
        return "redirect:/members";
    }

    /*
    로그인
     */
    @GetMapping("/members")
    public String loginForm(Model model){
        model.addAttribute("loginForm", new LoginForm());
        return "members/loginForm";
    }

    @PostMapping("/members")
    public String login(@Valid @ModelAttribute LoginForm form
            , BindingResult bindingResult
            , HttpServletRequest request){

        //이메일 또는 비밀번호를 누락시
        if (bindingResult.hasErrors()) {
            log.info("error={}", bindingResult);
            return "members/loginForm";
        }

        Member loginMember = memberService.login(form.getEmail(), form.getPassword());
        log.info("login? {}", loginMember);

        if (loginMember == null) {
            bindingResult.reject("loginfail", "이메일 또는 비밀번호가 맞지 않습니다.");
            return "members/loginForm";
        }
    /*
    세션
    */
        HttpSession session = request.getSession();  //만약 세션이 있으면 기존 세션을 반환하고, 없으면 신규 세션 생성
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);  //세션에 회원 정보 보관

        return "redirect:/";
    }

    /*
    로그아웃
    */
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    @GetMapping("/members/find-email")
    public String findEmailForm(Model model) {
        return "members/findEmail";
    }
    @GetMapping("/members/find-password")
    public String findPasswordForm(Model model) {
        return "members/findPassword";
    }

    @GetMapping("/api/members/check-email")
    @ResponseBody
    public Map<String, Boolean> checkEmailDuplicate(@RequestParam("email") String email) {
        boolean isAvailable = memberService.isEmailAvailable(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isAvailable", isAvailable);
        return response;
    }

    @GetMapping("/api/members/check-phone")
    @ResponseBody
    public Map<String, Boolean> checkPhoneDuplicate(@RequestParam("phoneNumber") String phoneNumber) {
        boolean isAvailable = memberService.isPhoneAvailable(phoneNumber);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isAvailable", isAvailable);
        return response;
    }

    @PostMapping("/members/find-email")
    public String findEmail(@Valid @ModelAttribute FindEmailRequestDto requestDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "members/findEmail";
        }
        try {
            String email = memberService.findEmailByBirthDateAndPhone(requestDto.getBirthDate(),requestDto.getPhoneNumber());
            model.addAttribute("foundEmail", email);
            return "members/findEmailResult";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", "일치하는 회원정보가 없습니다.");
            return "members/findEmail";
        }
    }

    @PostMapping("/members/find-password")
    public String findPassword(@Valid @ModelAttribute FindPasswordRequestDto requestDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "members/findPassword";
        }
        try {
            memberService.sendTemporaryPassword(requestDto.getEmail(),requestDto.getPhoneNumber());
            return "members/findPasswordResult";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage","일치하는 회원정보가 없습니다.");
            return "members/findPassword";
        }
    }
}



