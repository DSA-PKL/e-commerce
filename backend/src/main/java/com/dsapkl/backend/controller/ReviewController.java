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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.dsapkl.backend.controller.CartController.getMember;

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
            Member member = getMember(request);
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
                                        @RequestParam("content") String content,
                                        @RequestParam("rating") int rating,
                                        @RequestParam(value = "reviewImages", required = false) List<MultipartFile> reviewImages,
                                        @RequestParam("itemId") Long itemId,
                                        HttpServletRequest request) {
        try {
            Member member = getMember(request);
            if (member == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login required.");
            }

            ReviewRequestDto requestDto = ReviewRequestDto.builder()
                .itemId(itemId)
                .content(content)
                .rating(rating)
                .build();

            reviewService.updateReview(reviewId, requestDto, member.getId(), reviewImages);
            Review updatedReview = reviewService.findById(reviewId);
            return ResponseEntity.ok(ReviewResponseDto.from(updatedReview, member.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
    public ResponseEntity<List<ReviewResponseDto>> getItemReviews(@PathVariable Long itemId, HttpServletRequest request) {
        Member member = getMember(request);
        Long currentMemberId = member != null ? member.getId() : null;
        List<ReviewResponseDto> reviews = reviewService.getItemReviews(itemId, currentMemberId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/api/reviews/{reviewId}")
    @ResponseBody
    public ResponseEntity<ReviewResponseDto> getReview(@PathVariable Long reviewId, HttpServletRequest request) {
        Review review = reviewService.findById(reviewId);
        Member member = getMember(request);
        Long currentMemberId = member != null ? member.getId() : null;
        return ResponseEntity.ok(ReviewResponseDto.from(review, currentMemberId));
    }

    @PostMapping("/api/items/{itemId}/reviews")
    @ResponseBody
    public ResponseEntity<?> createReview(@PathVariable Long itemId,
                                        @RequestParam("rating") int rating,
                                        @RequestParam("content") String content,
                                        @RequestParam(value = "reviewImages", required = false) List<MultipartFile> reviewImages,
                                        HttpServletRequest request) {
        Member member = getMember(request);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login required.");
        }

        try {
            ReviewRequestDto requestDto = ReviewRequestDto.builder()
                .itemId(itemId)
                .rating(rating)
                .content(content)
                .build();
            Long reviewId = reviewService.createReview(requestDto, member.getId(), reviewImages);
            return ResponseEntity.ok(reviewId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 