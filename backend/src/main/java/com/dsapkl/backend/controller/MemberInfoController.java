package com.dsapkl.backend.controller;

import com.dsapkl.backend.dto.MemberInfoCreateDto;
import com.dsapkl.backend.dto.MemberInfoResponseDto;
import com.dsapkl.backend.entity.MemberInfo;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.service.MemberInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("user/api/member-info")
@RequiredArgsConstructor
public class MemberInfoController {

    private final MemberInfoService memberInfoService;

    @GetMapping("/update/{memberId}")
    public String updateMemberInfoForm(@PathVariable Long memberId, Model model) {
        MemberInfo memberInfo = memberInfoService.findMemberInfo(memberId);
        Member member = memberInfo.getMember();
        
        MemberInfoCreateDto memberInfoCreateDto = new MemberInfoCreateDto();
        memberInfoCreateDto.setGender(memberInfo.getGender());
        memberInfoCreateDto.setInterests(memberInfo.getInterests());
        
        model.addAttribute("memberInfo", MemberInfoResponseDto.of(memberInfo));
        model.addAttribute("member", member);
        model.addAttribute("memberInfoCreateDto", memberInfoCreateDto);
        return "members/updateMemberInfoForm";
    }

    @PostMapping("/update/{memberId}")
    public String updateMemberInfo(@PathVariable Long memberId, 
                                 @Valid @ModelAttribute MemberInfoCreateDto memberInfoCreateDto,
                                 BindingResult result,
                                 Model model) {
        try {
            if (result.hasErrors()) {
                MemberInfo memberInfo = memberInfoService.findMemberInfo(memberId);
                model.addAttribute("memberInfo", MemberInfoResponseDto.of(memberInfo));
                return "members/updateMemberInfoForm";
            }

            memberInfoService.updateMemberInfo(memberId, memberInfoCreateDto);
            return "redirect:/";
        } catch (Exception e) {
            MemberInfo memberInfo = memberInfoService.findMemberInfo(memberId);
            model.addAttribute("memberInfo", MemberInfoResponseDto.of(memberInfo));
            model.addAttribute("errorMessage", e.getMessage());
            return "members/updateMemberInfoForm";
        }
    }
} 