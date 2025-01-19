package com.dsapkl.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collection;

@Controller
public class GlobalController {

    @ModelAttribute("userRole")
    public String getUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                System.out.println(authority.getAuthority());
                return authority.getAuthority();
            }
        }
        return "ROLE_GUEST";
    }

    @GetMapping("/redirectByRole")
    public String redirectByRole(@ModelAttribute("userRole") String userRole) {

        if ("ADMIN".equals(userRole)) {
            return "redirect:/admin";
        } else if ("USER".equals(userRole)) {
            return "redirect:/user";
        }
        return "redirect:/guest";
    }

    @GetMapping("/")
    public String redirect() {
        return "redirect:/guest";
    }


}
