package com.dsapkl.backend.service;

import com.dsapkl.backend.dto.MemberInfoCreateDto;
import com.dsapkl.backend.entity.Category;
import com.dsapkl.backend.entity.MemberInfo;
import com.dsapkl.backend.repository.MemberInfoRepository;
import com.dsapkl.backend.repository.MemberRepository;
import com.dsapkl.backend.repository.OrderItemRepository;
import com.dsapkl.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberInfoService {

    private final MemberInfoRepository memberInfoRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public void updateMemberLoginInfo(Long memberId) {
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member does not exist."));
        
        memberInfo.updateLoginDays(LocalDate.now());
    }

    @Transactional
    public void updatePurchaseStatistics(Long memberId) {
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member does not exist."));

        Integer purchaseFrequency = orderRepository.findPurchaseFrequencyByMemberId(memberId);
        Integer averageOrderValue = orderRepository.findAverageOrderValueByMemberId(memberId);
        Integer totalSpending = orderRepository.findTotalSpendingByMemberId(memberId);

        memberInfo.updatePurchaseStatistics(purchaseFrequency, averageOrderValue, totalSpending);
    }

    @Transactional
    public void updateProductPreference(Long memberId) {
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member does not exist."));

        Category preferredCategory = orderItemRepository.findMostFrequentCategoryByMemberId(memberId);
        
        if (preferredCategory != null) {
            memberInfo.updateProductCategoryPreference(preferredCategory);
        }
    }

    // 모든 통계 한번에 업데이트
    @Transactional
    public void updateAllStatistics(Long memberId) {
        MemberInfo memberInfo = findMemberInfo(memberId);
        
        // 로그인 정보 업데이트
        memberInfo.updateLoginDays(LocalDate.now());

        // 구매 통계 업데이트
        Integer purchaseFrequency = orderRepository.findPurchaseFrequencyByMemberId(memberId);
        Integer averageOrderValue = orderRepository.findAverageOrderValueByMemberId(memberId);
        Integer totalSpending = orderRepository.findTotalSpendingByMemberId(memberId);
        memberInfo.updatePurchaseStatistics(purchaseFrequency, averageOrderValue, totalSpending);

        // 선호 카테고리 업데이트
        Category preferredCategory = orderItemRepository.findMostFrequentCategoryByMemberId(memberId);
        if (preferredCategory != null) {
            memberInfo.updateProductCategoryPreference(preferredCategory);
        }

        memberInfoRepository.save(memberInfo);
    }

    public MemberInfo findMemberInfo(Long memberId) {
        return memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member information does not exist."));
    }

    @Transactional
    public void updateMemberInfo(Long memberId, MemberInfoCreateDto dto) {
        MemberInfo memberInfo = findMemberInfo(memberId);
        
        // 기본 정보 업데이트
        if (dto.getAge() != null) {
            memberInfo.updateAge(dto.getAge());
        }
        if (dto.getGender() != null) {
            memberInfo.updateGender(dto.getGender());
        }
        if (dto.getInterests() != null) {
            memberInfo.updateInterests(dto.getInterests());
        }

        // 통계 정보가 없는 경우 초기화
        if (memberInfo.getIncome() == null || memberInfo.getLocation() == null) {
            memberInfo.initializeStatistics();
        }
        
        // 모든 통계 정보 업데이트
        updateAllStatistics(memberId);
        
        memberInfoRepository.save(memberInfo);
    }
} 