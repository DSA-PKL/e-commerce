package com.dsapkl.backend.controller;

import com.dsapkl.backend.entity.MemberInfo;
import com.dsapkl.backend.repository.MemberInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/clusters")
public class ClusterController {

    private final MemberInfoRepository memberInfoRepository;

    @GetMapping
    public String listClusters(Model model) {
        List<Integer> clusterNumbers = memberInfoRepository.findDistinctClusterNumbers();
        // null 값을 0으로 변환
        clusterNumbers = clusterNumbers.stream()
                .map(num -> num == null ? 0 : num)
                .sorted()
                .collect(Collectors.toList());
        model.addAttribute("clusterNumbers", clusterNumbers);
        return "clusters/clusterList";
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