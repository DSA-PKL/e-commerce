package com.dsapkl.backend.controller;

import com.dsapkl.backend.dto.MemberInfoCreateDto;
import com.dsapkl.backend.dto.MemberInfoResponseDto;
import com.dsapkl.backend.entity.Address;
import com.dsapkl.backend.entity.MemberInfo;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.service.MemberInfoService;
import com.dsapkl.backend.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("api/member-info")
@RequiredArgsConstructor
@Slf4j
public class MemberInfoController {

    private final MemberInfoService memberInfoService;
    private final MemberService memberService;

    @GetMapping("/update/{memberId}")
    public String updateMemberInfoForm(@PathVariable Long memberId, Model model) {
        log.info("Accessing update form for member ID: {}", memberId);
        
        try {
            MemberInfo memberInfo = memberInfoService.findMemberInfo(memberId);
            log.info("Found MemberInfo: {}", memberInfo);
            
            Member member = memberInfo.getMember();
            log.info("Found Member: {}", member);
            
            // DTO 생성 및 현재 정보로 초기화
            MemberInfoCreateDto memberInfoCreateDto = MemberInfoCreateDto.from(member, memberInfo);
            log.info("Created DTO: {}", memberInfoCreateDto);
            
            model.addAttribute("memberInfoCreateDto", memberInfoCreateDto);
            model.addAttribute("memberId", memberId);
            
            return "members/updateMemberInfoForm";
        } catch (Exception e) {
            log.error("Error in updateMemberInfoForm: ", e);
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/update/{memberId}")
    public String updateMemberInfo(@PathVariable Long memberId, 
                                 @Valid @ModelAttribute MemberInfoCreateDto memberInfoCreateDto,
                                 BindingResult result,
                                 Model model,
                                 HttpServletRequest request) {
        log.info("Updating member info for ID: {}", memberId);
        log.info("Received DTO: {}", memberInfoCreateDto);
        
        try {
            if (result.hasErrors()) {
                log.error("Validation errors: {}", result.getAllErrors());
                return "members/updateMemberInfoForm";
            }

            // Member 정보 업데이트
            Address address = new Address(
                memberInfoCreateDto.getCity(),
                memberInfoCreateDto.getStreet(),
                memberInfoCreateDto.getZipcode()
            );

            memberService.updateMember(memberId, 
                memberInfoCreateDto.getName(),
                memberInfoCreateDto.getEmail(),
                memberInfoCreateDto.getPhoneNumber(),
                memberInfoCreateDto.getBirthDate(),
                address
            );

            // MemberInfo 정보 업데이트
            memberInfoService.updateMemberInfo(memberId, memberInfoCreateDto);

            // 세션의 회원 정보 업데이트
            HttpSession session = request.getSession();
            Member updatedMember = memberService.findMember(memberId);
            session.setAttribute(SessionConst.LOGIN_MEMBER, updatedMember);

            return "redirect:/";
        } catch (Exception e) {
            log.error("Error updating member info: ", e);
            model.addAttribute("errorMessage", e.getMessage());
            return "members/updateMemberInfoForm";
        }
    }
} 