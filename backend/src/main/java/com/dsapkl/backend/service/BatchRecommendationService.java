package com.dsapkl.backend.service;

import com.dsapkl.backend.entity.MemberInfo;
import com.dsapkl.backend.entity.Cluster;
import com.dsapkl.backend.repository.MemberInfoRepository;
import com.dsapkl.backend.repository.ClusterRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchRecommendationService {

    private final MemberInfoRepository memberInfoRepository;
    private final ClusterRepository clusterRepository;
    private final ObjectMapper objectMapper;
    private static final String FLASK_URL = "http://localhost:5000/api/data";

    @Scheduled(fixedRate = 60000000, initialDelay = 60000000)
    public void processBatchPredictions() {
        log.info("Starting batch prediction process...");
        try {
            List<MemberInfo> allMembers = memberInfoRepository.findAll();

            for (MemberInfo memberInfo : allMembers) {
                try {
                    getClusterPrediction(memberInfo.getId());
                    log.info("Successfully processed member ID: {}", memberInfo.getId());
                } catch (Exception e) {
                    log.error("Error processing member ID: {}", memberInfo.getId(), e);
                }
            }

            log.info("Batch prediction process completed successfully");
        } catch (Exception e) {
            log.error("Error in batch prediction process", e);
        }
    }

    @Transactional
    public Integer getClusterPrediction(Long memberId) {
        MemberInfo memberInfo = memberInfoRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member does not exist."));

        Map<String, Object> memberData = prepareMemberData(memberInfo);

        String response = sendDataToFlask(memberData);

        Integer prediction = processFlaskResponse(response);

        Cluster cluster = new Cluster(prediction, prediction);
        clusterRepository.save(cluster);

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
            return predictionNode.get(0).asInt();
        } catch (Exception e) {
            throw new RuntimeException("Error processing Flask server response", e);
        }
    }
}