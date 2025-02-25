package com.dsapkl.backend.controller;

import com.dsapkl.backend.entity.UserActivityLog;
import com.dsapkl.backend.service.UserActivityLogService;
import com.dsapkl.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import com.dsapkl.backend.dto.UserActivityLogDTO;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {
    private final UserActivityLogService logService;
    private final MemberService memberService;

    @GetMapping("/logs")
    public ResponseEntity<?> getLogs(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam Long memberId) {
        
        try {
            if (!memberService.isAdmin(memberId)) {
                logService.logEvent(memberId, "ACCESS_DENIED", 
                    "Non-admin attempted to access admin dashboard");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
            }
            
            logService.logEvent(memberId, "ADMIN_ACTION", 
                String.format("Admin viewed logs from %s to %s", start, end));
            
            LocalDateTime startDate = LocalDateTime.parse(start);
            LocalDateTime endDate = LocalDateTime.parse(end);
            
            System.out.println("Searching logs between: " + startDate + " and " + endDate);
            
            List<UserActivityLogDTO> logs = logService.getActivityLogsByDateRange(startDate, endDate);
            System.out.println("Found logs: " + logs.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/log/search")
    public ResponseEntity<?> logSearchEvent(@RequestBody Map<String, Object> payload) {
        Long userId = Long.parseLong(payload.get("userId").toString());
        
        if (!memberService.isAdmin(userId)) {
            logService.logEvent(userId, "ACCESS_DENIED", 
                "Non-admin attempted to search logs");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        
        String startDate = payload.get("startDate").toString();
        String endDate = payload.get("endDate").toString();
        
        logService.logEvent(userId, "ADMIN_SEARCH", 
            String.format("Admin searched logs between %s and %s", startDate, endDate));
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/log/entry-click")
    public ResponseEntity<?> logEntryClick(@RequestBody Map<String, Object> payload) {
        Long userId = Long.parseLong(payload.get("userId").toString());
        
        if (!memberService.isAdmin(userId)) {
            logService.logEvent(userId, "ACCESS_DENIED", 
                "Non-admin attempted to view log details");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        
        String logId = payload.get("logId").toString();
        
        logService.logEvent(userId, "ADMIN_LOG_VIEW", 
            String.format("Admin viewed details of log entry %s", logId));
        
        return ResponseEntity.ok().build();
    }
} 