package com.dsapkl.backend.controller;

import com.dsapkl.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/members/manage")
@RequiredArgsConstructor
public class MemberManageController {

    private final MemberService memberService;

    @GetMapping("")
    public String manageMembers(Model model) {
        model.addAttribute("members", memberService.findAllMembers());
        return "members/memberManage";
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteMember(@PathVariable Long id) {
        try {
            memberService.deleteMember(id);
            return ResponseEntity.ok("Member deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 