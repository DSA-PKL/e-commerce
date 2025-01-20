package com.dsapkl.backend.controller;

import com.dsapkl.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/reviews/manage")
@RequiredArgsConstructor
public class ReviewManageController {

    private final ReviewService reviewService;

    @GetMapping("")
    public String manageReviews(Model model) {
        model.addAttribute("reviews", reviewService.findAllReviews());
        return "reviews/reviewManage";
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.ok("Review deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 