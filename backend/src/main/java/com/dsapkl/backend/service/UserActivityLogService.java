package com.dsapkl.backend.service;

import com.dsapkl.backend.dto.UserActivityLogDTO;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.UserActivityLog;
import com.dsapkl.backend.repository.UserActivityLogRepository;
import com.dsapkl.backend.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.dsapkl.backend.entity.Role;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityLogService {
    private final UserActivityLogRepository logRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    private boolean shouldLogActivity(Long userId) {
        return memberRepository.findById(userId)
            .map(member -> member.getRole() != Role.ADMIN)
            .orElse(false);
    }

    private void notifyLogUpdate(UserActivityLog log) {
        messagingTemplate.convertAndSend("/topic/logs", log);
    }

    public void logPageView(Long userId, String url) {
        log.info("Logging page view - userId: {}, url: {}", userId, url);
        
        if (!shouldLogActivity(userId)) {
            log.info("Skipping page view log for admin user");
            return;
        }
        
        UserActivityLog activityLog = new UserActivityLog();
        activityLog.setUserId(userId);
        activityLog.setActivityType("PAGE_VIEW");
        activityLog.setUrl(url);
        activityLog.setTimestamp(LocalDateTime.now());
        
        UserActivityLog savedLog = logRepository.save(activityLog);
        log.info("Page view logged successfully - logId: {}", savedLog.getId());
        notifyLogUpdate(savedLog);
    }

    public void logSearch(Long userId, String keyword, String clickedItemId) {
        if (!shouldLogActivity(userId)) return;
        
        UserActivityLog log = new UserActivityLog();
        log.setUserId(userId);
        log.setActivityType("SEARCH");
        log.setUrl("/search");
        log.setSearchKeyword(keyword);
        log.setItemId(clickedItemId);
        log.setAdditionalData(String.format("Searched for: %s", keyword));
        log.setTimestamp(LocalDateTime.now());
        logRepository.save(log);
        notifyLogUpdate(log);
    }

    public void logCartActivity(Long userId, String itemId, Integer quantity, String actionType) {
        if (!shouldLogActivity(userId)) return;
        
        UserActivityLog log = new UserActivityLog();
        log.setUserId(userId);
        log.setActivityType("CART");
        log.setItemId(itemId);
        log.setQuantity(quantity);
        log.setActionType(actionType);
        log.setTimestamp(LocalDateTime.now());
        logRepository.save(log);
        notifyLogUpdate(log);
    }

    public List<UserActivityLogDTO> getActivityLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        List<UserActivityLog> logs = logRepository.findByTimestampBetween(start, end);
        return logs.stream()
            .map(log -> {
                Member member = memberRepository.findById(log.getUserId())
                    .orElse(null);
                return new UserActivityLogDTO(log, member);
            })
            .collect(Collectors.toList());
    }

    public List<UserActivityLog> getActivityLogsByUser(Long userId) {
        return logRepository.findByUserId(userId);
    }

    public void logClick(Long userId, String elementId, String url, String context) {
        if (!shouldLogActivity(userId)) return;
        
        UserActivityLog log = new UserActivityLog();
        log.setUserId(userId);
        log.setActivityType("CLICK");
        log.setUrl(url);
        try {
            log.setAdditionalData(objectMapper.writeValueAsString(Map.of(
                "elementId", elementId,
                "context", context
            )));
        } catch (Exception e) {
            log.setAdditionalData("{}");
        }
        log.setTimestamp(LocalDateTime.now());
        logRepository.save(log);
        notifyLogUpdate(log);
    }

    public void logSession(Long userId, String sessionId, String action) {
        if (!shouldLogActivity(userId)) return;
        
        UserActivityLog log = new UserActivityLog();
        log.setUserId(userId);
        log.setActivityType("SESSION");
        log.setTimestamp(LocalDateTime.now());
        logRepository.save(log);
        notifyLogUpdate(log);
    }

    public void logEvent(Long userId, String eventType, String eventData) {
        if (!shouldLogActivity(userId)) return;
        
        UserActivityLog log = new UserActivityLog();
        log.setUserId(userId);
        log.setActivityType("EVENT");
        try {
            log.setAdditionalData(objectMapper.writeValueAsString(Map.of(
                "eventType", eventType,
                "eventData", eventData
            )));
        } catch (Exception e) {
            log.setAdditionalData("{}");
        }
        log.setTimestamp(LocalDateTime.now());
        logRepository.save(log);
        notifyLogUpdate(log);
    }

    public List<UserActivityLog> getClicksByElement(String elementId) {
        return logRepository.findByActivityTypeAndAdditionalDataContaining("CLICK", elementId);
    }

    public List<UserActivityLog> getSessionsByDateRange(LocalDateTime start, LocalDateTime end) {
        return logRepository.findByActivityTypeAndTimestampBetween("SESSION", start, end);
    }

    public List<UserActivityLog> getEventsByType(String eventType) {
        return logRepository.findByActivityTypeAndAdditionalDataContaining("EVENT", eventType);
    }

    public void logProductView(Long userId, Long productId, String productName) {
        if (!shouldLogActivity(userId)) return;
        
        UserActivityLog log = new UserActivityLog();
        log.setUserId(userId);
        log.setActivityType("PRODUCT_VIEW");
        log.setUrl("/products/" + productId);
        log.setAdditionalData(String.format("Viewed product: %s (ID: %d)", productName, productId));
        log.setTimestamp(LocalDateTime.now());
        logRepository.save(log);
        notifyLogUpdate(log);
    }

    public void logSearch(Long userId, String keyword) {
        if (!shouldLogActivity(userId)) return;
        
        UserActivityLog log = new UserActivityLog();
        log.setUserId(userId);
        log.setActivityType("SEARCH");
        log.setUrl("/search");
        log.setAdditionalData(String.format("Searched for: %s", keyword));
        log.setTimestamp(LocalDateTime.now());
        logRepository.save(log);
        notifyLogUpdate(log);
    }

    public void logCartActivity(Long userId, String action, Long productId, String productName, int quantity) {
        if (!shouldLogActivity(userId)) return;
        
        UserActivityLog log = new UserActivityLog();
        log.setUserId(userId);
        log.setActivityType("CART");
        log.setUrl("/cart");
        log.setAdditionalData(String.format("%s %d x %s (ID: %d) to cart", 
            action, quantity, productName, productId));
        log.setTimestamp(LocalDateTime.now());
        logRepository.save(log);
        notifyLogUpdate(log);
    }

    public void logPurchase(Long userId, Long orderId, List<String> productNames, double totalAmount) {
        if (!shouldLogActivity(userId)) return;
        
        UserActivityLog log = new UserActivityLog();
        log.setUserId(userId);
        log.setActivityType("PURCHASE");
        log.setUrl("/orders/" + orderId);
        log.setAdditionalData(String.format("Purchased: %s, Total: $%.2f", 
            String.join(", ", productNames), totalAmount));
        log.setTimestamp(LocalDateTime.now());
        logRepository.save(log);
        notifyLogUpdate(log);
    }
} 