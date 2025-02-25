package com.dsapkl.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class UserActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;  // User 엔티티 대신 userId만 저장
    private String activityType; // PAGE_VIEW, CART, SEARCH, PURCHASE 등
    private String url;
    private String pageId;
    private String itemId;
    private String searchKeyword;
    private Integer quantity;
    private Double price;
    private String actionType; // ADD, REMOVE, VIEW 등
    private LocalDateTime timestamp;
    
    @Column(columnDefinition = "TEXT")
    private String additionalData; // JSON 형태로 추가 데이터 저장
} 