package com.dsapkl.backend.dto;

import com.dsapkl.backend.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponseDto {
    private Long reviewId;
    private Long itemId;
    private Long memberId;
    private String memberName;
    private String content;
    private int rating;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @Builder
    public ReviewResponseDto(Long reviewId, Long itemId, Long memberId, String memberName,
                           String content, int rating, LocalDateTime createdDate, LocalDateTime modifiedDate) {
        this.reviewId = reviewId;
        this.itemId = itemId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.content = content;
        this.rating = rating;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }

    public static ReviewResponseDto from(Review review) {
        return ReviewResponseDto.builder()
                .reviewId(review.getId())
                .itemId(review.getItem().getId())
                .memberId(review.getMember().getId())
                .memberName(review.getMember().getName())
                .content(review.getContent())
                .rating(review.getRating())
                .createdDate(review.getCreatedDate())
                .modifiedDate(review.getModifiedDate())
                .build();
    }
} 