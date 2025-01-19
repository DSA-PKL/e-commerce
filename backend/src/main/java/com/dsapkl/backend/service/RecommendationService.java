package com.dsapkl.backend.service;

import com.dsapkl.backend.entity.MemberInfo;
import com.dsapkl.backend.repository.MemberInfoRepository;
import com.dsapkl.backend.entity.Cluster;
import com.dsapkl.backend.repository.ClusterRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final MemberInfoRepository memberInfoRepository;
    private final ClusterRepository clusterRepository;
    private final ObjectMapper objectMapper;
    private static final String FLASK_URL = "http://localhost:5000/api/data";

    public Integer getClusterPrediction(Long memberId) {
        // MemberInfo 조회
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member does not exist."));

        // 회원 데이터 가공
        Map<String, Object> memberData = prepareMemberData(memberInfo);

        // Flask 서버로 데이터 전송 및 응답 받기
        String response = sendDataToFlask(memberData);

        // 응답 처리 및 클러스터 저장
        Integer prediction = processFlaskResponse(response);
        
        // Cluster 엔티티 생성 및 저장
        Cluster cluster = new Cluster(prediction, prediction);
        clusterRepository.save(cluster);

        // MemberInfo에 cluster_id 업데이트
        memberInfo.updateCluster(cluster);
        memberInfoRepository.save(memberInfo);

        return prediction;
    }

    private Map<String, Object> prepareMemberData(MemberInfo memberInfo) {
        Map<String, Object> data = new HashMap<>();
        data.put("age", memberInfo.getAge());
        data.put("gender", memberInfo.getGender());
        data.put("location", memberInfo.getLocation());
        data.put("income", memberInfo.getIncome());
        data.put("lastLoginDays", memberInfo.getLastLoginDays());
        data.put("purchaseFrequency", memberInfo.getPurchaseFrequency());
        data.put("averageOrderValue", memberInfo.getAverageOrderValue() / 1400);
        data.put("totalSpending", memberInfo.getTotalSpending() / 1400);
        data.put("timeSpentOnSiteMinutes", memberInfo.getTimeSpentOnSiteMinutes());
        data.put("pagesViewed", memberInfo.getPagesViewed());
        data.put("newsletterSubscription", memberInfo.getNewsletterSubscription());
        data.put("interests", memberInfo.getInterests().toString());
        data.put("productCategoryPreference", memberInfo.getProductCategoryPreference().toString());
        
        return data;
    }

    private String sendDataToFlask(Map<String, Object> data) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                FLASK_URL,
                HttpMethod.POST,
                request,
                String.class
        );

        return response.getBody();
    }

    private Integer processFlaskResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode predictionNode = root.get("prediction");
            Integer prediction = predictionNode.get(0).asInt();
            return prediction;
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while processing Flask server response", e);
        }
    }
} 