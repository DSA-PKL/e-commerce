package com.dsapkl.backend.controller.dto;

import com.dsapkl.backend.entity.Category;
import com.dsapkl.backend.entity.Interest;
import com.dsapkl.backend.entity.MemberInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MemberInfoResponseDto {
    private final Long memberId;
    private final Integer age;
    private final String gender;
    private final String location;
    private final Integer income;
    private final LocalDate lastLoginDay;
    private final LocalDate loginDay;
    private final Integer lastLoginDays;
    private final Integer purchaseFrequency;
    private final Integer averageOrderValue;
    private final Integer totalSpending;
    private final Integer timeSpentOnSiteMinutes;
    private final Integer pagesViewed;
    private final Interest interests;
    private final Category productCategoryPreference;

    @Builder
    private MemberInfoResponseDto(MemberInfo memberInfo) {
        this.memberId = memberInfo.getMember().getId();
        this.age = memberInfo.getAge();
        this.gender = memberInfo.getGender();
        this.location = memberInfo.getLocation();
        this.income = memberInfo.getIncome();
        this.lastLoginDay = memberInfo.getLastLoginDay();
        this.loginDay = memberInfo.getLoginDay();
        this.lastLoginDays = memberInfo.getLastLoginDays();
        this.purchaseFrequency = memberInfo.getPurchaseFrequency();
        this.averageOrderValue = memberInfo.getAverageOrderValue();
        this.totalSpending = memberInfo.getTotalSpending();
        this.timeSpentOnSiteMinutes = memberInfo.getTimeSpentOnSiteMinutes();
        this.pagesViewed = memberInfo.getPagesViewed();
        this.interests = memberInfo.getInterests();
        this.productCategoryPreference = memberInfo.getProductCategoryPreference();
    }

    public static MemberInfoResponseDto of(MemberInfo memberInfo) {
        return new MemberInfoResponseDto(memberInfo);
    }
} 