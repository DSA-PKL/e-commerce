package com.dsapkl.backend.config;

import com.dsapkl.backend.service.UserActivityLogService;
import com.dsapkl.backend.controller.CartController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PageViewLoggingInterceptor implements HandlerInterceptor {
    private final UserActivityLogService logService;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, Exception ex) {
        var member = CartController.getMember(request);
        if (member != null) {
            log.info("Intercepting page view - userId: {}, uri: {}", member.getId(), request.getRequestURI());
            logService.logPageView(member.getId(), request.getRequestURI());
        }
    }
} 