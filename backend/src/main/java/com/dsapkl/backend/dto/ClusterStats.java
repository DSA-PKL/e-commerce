package com.dsapkl.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClusterStats {
    private Integer clusterNumber;
    private double avgAge;
    private String dominantGender;
    private String dominantLocation;
    private double avgIncome;
    private double avgLastLoginDays;
    private double avgPurchaseFreq;
    private double avgOrderValue;
    private double avgTotalSpending;
    private double avgTimeSpent;
    private double avgPagesViewed;
    private double newsletterRatio;  // 뉴스레터 구독 비율
    private String dominantInterest;
    private String dominantCategory;
} 