package com.dsapkl.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "Member_Info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "info_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private Integer age;
    private String gender;
    private String location; // 전부 "urban"으로 통일
    private Integer income; // 중앙값으로 통일

    private LocalDate lastLoginDay; // 로그인 시 기록, 없으면 null
    private LocalDate loginDay; // 로그인 시 기록
    private Integer lastLoginDays; // lastLoginDay와 loginDay의 차이

    private Integer purchaseFrequency; // order 테이블에서 가져온 정보
    private Integer averageOrderValue; // order 테이블에서 평균값 계산
    private Integer totalSpending; // order 테이블에서 합계 계산

    private Integer timeSpentOnSiteMinutes; // 중앙값으로 통일
    private Integer pagesViewed; // 중앙값으로 통일
    private Integer newsletterSubscription; // 그냥 전부 수신 동의로 설정

    @Enumerated(value = EnumType.STRING)
    private Interest interests;

    @Enumerated(value = EnumType.STRING)
    private Category productCategoryPreference; // order_item 테이블에서 가져옴

    @ManyToOne
    @JoinColumn(name = "cluster_id")
    private Cluster cluster_id; // 파이썬 군집 결과를 저장

    @Builder
    private MemberInfo(Member member, Integer age, String gender, Interest interests,
                       LocalDate lastLoginDay, LocalDate loginDay, Integer lastLoginDays, Integer purchaseFrequency,
                       Integer averageOrderValue, Integer totalSpending, Category productCategoryPreference,
                       Cluster cluster_id
    ) {
        this.member = member;
        this.age = age;
        this.gender = gender;
        this.location = "Urban";
        this.income = 81042;
        this.interests = interests;
        this.lastLoginDay = lastLoginDay;
        this.loginDay = loginDay;
        this.lastLoginDays = lastLoginDays;
        this.purchaseFrequency = purchaseFrequency;
        this.averageOrderValue = averageOrderValue;
        this.totalSpending = totalSpending;
        this.productCategoryPreference = productCategoryPreference;
        this.timeSpentOnSiteMinutes = 293;
        this.pagesViewed = 25;
        this.newsletterSubscription = 1;
        this.cluster_id = cluster_id;
    }

    private MemberInfo(Member member) {
        this.member = member;
        this.location = "Urban";
        this.income = 81042;
        this.timeSpentOnSiteMinutes = 293;
        this.pagesViewed = 25;
        this.newsletterSubscription = 1;
        
        this.purchaseFrequency = 0;
        this.averageOrderValue = 0;
        this.totalSpending = 0;
        
        this.loginDay = LocalDate.now();
        this.lastLoginDay = null;
        this.lastLoginDays = 0;
    }

    public static MemberInfo createMemberInfo(Member member) {
        return new MemberInfo(member);
    }

    public void updateLoginDays(LocalDate loginDay) {
        this.lastLoginDay = this.loginDay;
        this.loginDay = loginDay;
        if (this.lastLoginDay != null) {
            this.lastLoginDays = (int) java.time.temporal.ChronoUnit.DAYS.between(this.lastLoginDay, this.loginDay);
        } else {
            this.lastLoginDays = 1;
        }
    }

    public void updatePurchaseStatistics(Integer purchaseFrequency, Integer averageOrderValue, Integer totalSpending) {
        this.purchaseFrequency = purchaseFrequency;
        this.averageOrderValue = averageOrderValue;
        this.totalSpending = totalSpending;
    }

    public void updateProductCategoryPreference(Category productCategoryPreference) {
        this.productCategoryPreference = productCategoryPreference;
    }

    public void updateAge(Integer age) {
        this.age = age;
    }

    public void updateGender(String gender) {
        this.gender = gender;
    }

    public void updateInterests(Interest interests) {
        this.interests = interests;
    }

    public void initializeStatistics() {
        this.location = "Urban";
        this.income = 81042;
        this.timeSpentOnSiteMinutes = 293;
        this.pagesViewed = 25;
        this.newsletterSubscription = 1;
        this.purchaseFrequency = 0;
        this.averageOrderValue = 0;
        this.totalSpending = 0;
    }

    public void updateCluster(Cluster cluster_id) {this.cluster_id = cluster_id;}

}