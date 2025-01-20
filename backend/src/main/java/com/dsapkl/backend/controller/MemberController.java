package com.dsapkl.backend.controller;

import com.dsapkl.backend.dto.*;
import com.dsapkl.backend.entity.Address;
import com.dsapkl.backend.entity.Interest;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.Role;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.MemberInfoService;
import com.dsapkl.backend.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final MemberInfoService memberInfoService;

    private final CartService cartService;

    /*
    회원가입
     */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        model.addAttribute("roleCodes", Role.values());  // ADMIN, USER 선택 가능
        return "members/createMemberForm";
    }

    @Data
    @AllArgsConstructor
    static class RoleCode {
        private String code;
        private String displayName;
    }

    //회원가입
    @PostMapping("/new")
    public String createMember(@Valid @ModelAttribute MemberForm memberForm,
                               BindingResult bindingResult, Model model,
                               @RequestParam("role") String role,
                               @RequestParam("gender") String gender,
                               @RequestParam("interests") String interests) {

        //memberForm 객체에 binding 했을 때 에러
        if(bindingResult.hasErrors()) {
            List<RoleCode> roleCodes = new ArrayList<>();
            roleCodes.add(new RoleCode("admin", "Seller"));
            roleCodes.add(new RoleCode("user", "Buyer"));
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

            Long savedMember = member.getId();
            
            // 생년월일로 나이 계산
            String birthDateStr = memberForm.getBirthDate();
            int birthYear = Integer.parseInt(birthDateStr.substring(0, 4));
            int currentYear = LocalDate.now().getYear();
            int age = currentYear - birthYear;
            
            // MemberInfo 생성 및 저장
            MemberInfoCreateDto memberInfoDto = new MemberInfoCreateDto();
            memberInfoDto.setAge(age);
            memberInfoDto.setGender(gender);
            memberInfoDto.setInterests(Interest.valueOf(interests));
            
            memberInfoService.updateMemberInfo(savedMember, memberInfoDto);
            
        } catch (IllegalStateException e){
            model.addAttribute("errorMessage", e.getMessage());
            return "members/createMemberForm";
        }
        return "redirect:/members";
    }

    /*
    로그인
     */
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "members/loginForm";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm form,
                       BindingResult bindingResult,
                       HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return "members/loginForm";
        }

        try {
            Member loginMember = memberService.login(form.getEmail(), form.getPassword());
            if (loginMember == null) {
                bindingResult.reject("loginFail", "Invalid email or password");
                return "members/loginForm";
            }

            HttpSession session = request.getSession();
            session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);
            return "redirect:/";
        } catch (Exception e) {
            bindingResult.reject("loginError", "An error occurred during login");
            return "members/loginForm";
        }
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

    @GetMapping("/find-email")
    public String findEmailForm(Model model) {
        return "members/findEmail";
    }
    @GetMapping("/find-password")
    public String findPasswordForm(Model model) {
        return "members/findPassword";
    }

    @GetMapping("/api/check-email")
    @ResponseBody
    public Map<String, Boolean> checkEmailDuplicate(@RequestParam("email") String email) {
        boolean isAvailable = memberService.isEmailAvailable(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isAvailable", isAvailable);
        return response;
    }

    @GetMapping("/api/check-phone")
    @ResponseBody
    public Map<String, Boolean> checkPhoneDuplicate(@RequestParam("phoneNumber") String phoneNumber) {
        boolean isAvailable = memberService.isPhoneAvailable(phoneNumber);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isAvailable", isAvailable);
        return response;
    }

    @PostMapping("/find-email")
    public String findEmail(@Valid @ModelAttribute FindEmailRequestDto requestDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "members/findEmail";
        }
        try {
            String email = memberService.findEmailByBirthDateAndPhone(requestDto.getBirthDate(),requestDto.getPhoneNumber());
            model.addAttribute("foundEmail", email);
            return "members/findEmailResult";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", "No matching member information found.");
            return "members/findEmail";
        }
    }

    @PostMapping("/find-password")
    public String findPassword(@Valid @ModelAttribute FindPasswordRequestDto requestDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "members/findPassword";
        }
        try {
            memberService.sendTemporaryPassword(requestDto.getEmail(),requestDto.getPhoneNumber());
            return "members/findPasswordResult";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", "No matching member information found.");
            return "members/findPassword";
        }
    }

    // 관리자용 회원 관리 페이지
    @GetMapping("/manage")
    public String manageMember(Model model) {
        List<Member> members = memberService.findAllMembers();
        model.addAttribute("members", members);
        return "members/memberManage";
    }
    
    // 관리자용 회원 삭제
    @PostMapping("/manage/delete/{id}")
    public String deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return "redirect:/members/manage";
    }
}



