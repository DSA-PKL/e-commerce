package com.dsapkl.backend.controller;

import com.dsapkl.backend.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DataController {

    private final RecommendationService recommendationService;

    @GetMapping("/send-member-data/{memberId}")
    public Integer sendMemberDataToFlask(@PathVariable Long memberId) {
        return recommendationService.getClusterPrediction(memberId);
    }
}
