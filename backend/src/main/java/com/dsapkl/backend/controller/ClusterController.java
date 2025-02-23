package com.dsapkl.backend.controller;

import com.dsapkl.backend.entity.MemberInfo;
import com.dsapkl.backend.repository.MemberInfoRepository;
import com.dsapkl.backend.dto.ClusterStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/clusters")
public class ClusterController {

    private final MemberInfoRepository memberInfoRepository;

    @GetMapping
    public String listClusters(Model model) {
        List<Integer> clusterNumbers = memberInfoRepository.findDistinctClusterNumbers();
        List<ClusterStats> clusterStats = new ArrayList<>();
        
        for (Integer clusterId : clusterNumbers) {
            List<MemberInfo> members = clusterId == 0 ? 
                memberInfoRepository.findMembersWithNullCluster() :
                memberInfoRepository.findByClusterId(clusterId);
                
            // 통계 계산
            double avgAge = members.stream().mapToInt(MemberInfo::getAge).average().orElse(0);
            String dominantGender = calculateMode(members.stream().map(MemberInfo::getGender).collect(Collectors.toList()));
            String dominantLocation = calculateMode(members.stream().map(MemberInfo::getLocation).collect(Collectors.toList()));
            double avgIncome = members.stream().mapToInt(MemberInfo::getIncome).average().orElse(0);
            double avgLastLoginDays = members.stream().mapToInt(MemberInfo::getLastLoginDays).average().orElse(0);
            double avgPurchaseFreq = members.stream().mapToInt(MemberInfo::getPurchaseFrequency).average().orElse(0);
            double avgOrderValue = members.stream().mapToInt(MemberInfo::getAverageOrderValue).average().orElse(0);
            double avgTotalSpending = members.stream().mapToInt(MemberInfo::getTotalSpending).average().orElse(0);
            double avgTimeSpent = members.stream().mapToInt(MemberInfo::getTimeSpentOnSiteMinutes).average().orElse(0);
            double avgPagesViewed = members.stream().mapToInt(MemberInfo::getPagesViewed).average().orElse(0);
            double newsletterRatio = (double) members.stream().filter(m -> m.getNewsletterSubscription() == 1).count() / members.size();
            String dominantInterest = calculateMode(members.stream().map(m -> m.getInterests().toString()).collect(Collectors.toList()));
            String dominantCategory = calculateMode(members.stream().map(m -> m.getProductCategoryPreference().toString()).collect(Collectors.toList()));
            
            clusterStats.add(ClusterStats.builder()
                .clusterNumber(clusterId)
                .avgAge(avgAge)
                .dominantGender(dominantGender)
                .dominantLocation(dominantLocation)
                .avgIncome(avgIncome)
                .avgLastLoginDays(avgLastLoginDays)
                .avgPurchaseFreq(avgPurchaseFreq)
                .avgOrderValue(avgOrderValue)
                .avgTotalSpending(avgTotalSpending)
                .avgTimeSpent(avgTimeSpent)
                .avgPagesViewed(avgPagesViewed)
                .newsletterRatio(newsletterRatio)
                .dominantInterest(dominantInterest)
                .dominantCategory(dominantCategory)
                .build());
        }
        
        model.addAttribute("clusterStats", clusterStats);
        return "clusters/clusterList";
    }

    private String calculateMode(List<String> values) {
        return values.stream()
            .filter(v -> v != null)
            .collect(Collectors.groupingBy(v -> v, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
    }

    @GetMapping("/{clusterId}/members")
    public String listClusterMembers(@PathVariable Integer clusterId, Model model) {
        List<MemberInfo> members = clusterId == 0 ? 
            memberInfoRepository.findMembersWithNullCluster() :
            memberInfoRepository.findByClusterId(clusterId);

        // 통계 데이터 계산
        double avgAge = members.stream()
            .mapToInt(MemberInfo::getAge)
            .average()
            .orElse(0);
        
        double avgPurchase = members.stream()
            .mapToInt(MemberInfo::getPurchaseFrequency)
            .average()
            .orElse(0);

        model.addAttribute("clusterId", clusterId);
        model.addAttribute("members", members);
        model.addAttribute("avgAge", avgAge);
        model.addAttribute("avgPurchase", avgPurchase);
        
        return "clusters/clusterMembers";
    }
} 