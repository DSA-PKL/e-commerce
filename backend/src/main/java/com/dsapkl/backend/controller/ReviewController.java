package com.dsapkl.backend.controller;

import com.dsapkl.backend.dto.ReviewRequestDto;
import com.dsapkl.backend.dto.ReviewResponseDto;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.Review;
import com.dsapkl.backend.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.dsapkl.backend.controller.CartController.getMember;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/reviews")
    @ResponseBody
    public ResponseEntity<?> createReview(@Valid @RequestBody ReviewRequestDto requestDto,
                                        HttpServletRequest request) {
        try {
            Member member = getMember(request);
            if (member == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("로그인이 필요합니다.");
            }
            Long reviewId = reviewService.createReview(requestDto, member.getId());
            return ResponseEntity.ok(reviewId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/api/reviews/{reviewId}")
    @ResponseBody
    public ResponseEntity<Void> updateReview(@PathVariable Long reviewId,
                                           @Valid @RequestBody ReviewRequestDto requestDto,
                                           HttpServletRequest request) {
        Member member = getMember(request);
        reviewService.updateReview(reviewId, requestDto, member.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/reviews/{reviewId}")
    @ResponseBody
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId,
                                           HttpServletRequest request) {
        Member member = getMember(request);
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