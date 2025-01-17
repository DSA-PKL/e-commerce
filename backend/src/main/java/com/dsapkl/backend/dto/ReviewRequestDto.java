package com.dsapkl.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequestDto {
    private Long itemId;
    @NotBlank(message = "리뷰 내용을 입력해주세요.")
    @Size(min = 1, max = 1000, message = "리뷰는 1000자 이하로 작성해주세요.")
    private String content;
    
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다")
    private int rating;
} 