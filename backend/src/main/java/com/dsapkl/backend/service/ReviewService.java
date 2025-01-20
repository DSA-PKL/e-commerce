package com.dsapkl.backend.service;

import com.dsapkl.backend.entity.Review;
import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.ReviewImage;
import com.dsapkl.backend.repository.ItemRepository;
import com.dsapkl.backend.repository.MemberRepository;
import com.dsapkl.backend.repository.ReviewImageRepository;
import com.dsapkl.backend.repository.ReviewRepository;
import com.dsapkl.backend.dto.ReviewRequestDto;
import com.dsapkl.backend.dto.ReviewResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final FileHandler fileHandler;
    private final ReviewImageRepository reviewImageRepository;

    // 리뷰 작성
    @Transactional
    public Long createReview(ReviewRequestDto requestDto, Long memberId, List<MultipartFile> images) throws IOException {
        Item item = itemRepository.findById(requestDto.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Product does not exist."));
        
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member does not exist."));

        // 이미 리뷰를 작성했는지 확인
        if (reviewRepository.findByItemIdAndMemberId(item.getId(), member.getId()).isPresent()) {
            throw new IllegalStateException("You have already written a review.");
        }

        Review review = Review.builder()
                .item(item)
                .member(member)
                .content(requestDto.getContent())
                .rating(requestDto.getRating())
                .build();

        // 이미지 처리
        if (images != null && !images.isEmpty()) {
            List<ReviewImage> reviewImages = fileHandler.storeFiles(images);
            for (ReviewImage image : reviewImages) {
                review.addReviewImage(image);
            }
        }

        return reviewRepository.save(review).getId();
    }

    // 리뷰 수정
    @Transactional
    public void updateReview(Long reviewId, ReviewRequestDto requestDto, Long memberId, List<MultipartFile> images) throws IOException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review does not exist."));

        if (!review.getMember().getId().equals(memberId)) {
            throw new IllegalStateException("Only the author can update the review.");
        }

        // 새 이미지가 있는 경우에만 기존 이미지 삭제 처리
        if (images != null && !images.isEmpty()) {
            // 기존 이미지들을 물리적으로 삭제
            List<ReviewImage> existingImages = reviewImageRepository.findByReviewIdAndDeleteYN(reviewId, "N");
            reviewImageRepository.deleteAll(existingImages);
            review.getReviewImages().clear();  // 리뷰의 이미지 컬렉션 초기화

            // 새 이미지 추가
            List<ReviewImage> reviewImages = fileHandler.storeFiles(images);
            for (ReviewImage image : reviewImages) {
                review.addReviewImage(image);
            }
        }

        review.update(requestDto.getContent(), requestDto.getRating());
    }

    // 리뷰 삭제
    public void deleteReview(Long reviewId, Long memberId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review does not exist."));

        if (!review.getMember().getId().equals(memberId)) {
            throw new IllegalStateException("Only the author can delete the review.");
        }

        reviewRepository.delete(review);
    }

    // 상품의 모든 리뷰 조회
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getItemReviews(Long itemId) {
        return reviewRepository.findByItemIdOrderByCreatedDateDesc(itemId).stream()
                .map(ReviewResponseDto::from)
                .collect(Collectors.toList());
    }

    // 상품의 평균 평점 조회
    @Transactional(readOnly = true)
    public double getItemAverageRating(Long itemId) {
        return reviewRepository.findAverageRatingByItemId(itemId).orElse(0.0);
    }

    @Transactional(readOnly = true)
    public Review findById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review does not exist."));
    }

    // 회원의 모든 리뷰 조회
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getMemberReviews(Long memberId) {
        return reviewRepository.findByMemberIdOrderByCreatedDateDesc(memberId).stream()
                .map(ReviewResponseDto::from)
                .collect(Collectors.toList());
    }

    // 모든 리뷰 조회
    public List<Review> findAllReviews() {
        return reviewRepository.findAll();
    }

    // 리뷰 삭제 (관리자용)
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        reviewRepository.delete(review);
    }
} 