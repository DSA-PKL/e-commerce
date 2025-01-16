package com.dsapkl.backend.dto;

import com.dsapkl.backend.entity.Review;
import lombok.Builder;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ReviewResponseDto {
    private Long reviewId;
    private Long itemId;
    private Long memberId;
    private String memberName;
    private String content;
    private int rating;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime modifiedDate;
    private List<ReviewImageDto> reviewImages;

    public static ReviewResponseDto from(Review review) {
        return ReviewResponseDto.builder()
                .reviewId(review.getId())
                .itemId(review.getItem().getId())
                .memberId(review.getMember().getId())
                .memberName(review.getMember().getName())
                .content(review.getContent())
                .rating(review.getRating())
                .createdDate(review.getCreatedDate())
                .reviewImages(review.getReviewImages().stream()
                        .map(ReviewImageDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
} 