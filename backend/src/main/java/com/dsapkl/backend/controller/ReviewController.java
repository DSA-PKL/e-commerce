package com.dsapkl.backend.controller;

import com.dsapkl.backend.dto.ReviewRequestDto;
import com.dsapkl.backend.dto.ReviewResponseDto;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.Review;
import com.dsapkl.backend.service.ReviewService;
import com.dsapkl.backend.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/reviews")
    @ResponseBody
    public ResponseEntity<?> createReview(@Valid @ModelAttribute ReviewRequestDto requestDto,
                                        @RequestParam(required = false) List<MultipartFile> images,
                                        HttpServletRequest request) {
        try {
            Member member = SessionUtil.getMember(request);
            if (member == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Login required.");
            }
            Long reviewId = reviewService.createReview(requestDto, member.getId(), images);
            return ResponseEntity.ok(reviewId);
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/api/reviews/{reviewId}")
    @ResponseBody
    public ResponseEntity<?> updateReview(@PathVariable Long reviewId,
                                        @Valid @ModelAttribute ReviewRequestDto requestDto,
                                        @RequestParam(required = false) List<MultipartFile> images,
                                        HttpServletRequest request) {
        try {
            Member member = SessionUtil.getMember(request);
            if (member == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Login required.");
            }
            reviewService.updateReview(reviewId, requestDto, member.getId(), images);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/api/reviews/{reviewId}")
    @ResponseBody
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId,
                                           HttpServletRequest request) {
        Member member = SessionUtil.getMember(request);
        reviewService.deleteReview(reviewId, member.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/items/{itemId}/reviews")
    @ResponseBody
    public ResponseEntity<List<ReviewResponseDto>> getItemReviews(@PathVariable Long itemId) {
        return ResponseEntity.ok(reviewService.getItemReviews(itemId));
    }

    @GetMapping("/api/reviews/{reviewId}")
    @ResponseBody
    public ResponseEntity<ReviewResponseDto> getReview(@PathVariable Long reviewId) {
        Review review = reviewService.findById(reviewId);
        return ResponseEntity.ok(ReviewResponseDto.from(review));
    }
} 