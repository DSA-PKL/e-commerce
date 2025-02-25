package com.dsapkl.backend.controller;

import com.dsapkl.backend.service.MemberService;
import com.dsapkl.backend.service.UserActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AdminViewController {
    private final MemberService memberService;
    private final UserActivityLogService logService;

    @GetMapping("/admin/analytics")
    public String adminAnalytics(@RequestParam Long memberId, Model model) {
        logService.logPageView(memberId, "/admin/analytics");
        model.addAttribute("memberId", memberId);
        return "admin/analytics";
    }
} 