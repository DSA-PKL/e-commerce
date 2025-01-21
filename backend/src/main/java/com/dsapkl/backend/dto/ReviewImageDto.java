package com.dsapkl.backend.dto;

import com.dsapkl.backend.entity.ReviewImage;
import lombok.Getter;

@Getter
public class ReviewImageDto {
    private Long id;
    private String imageUrl;

    public ReviewImageDto(ReviewImage reviewImage) {
        this.id = reviewImage.getId();
        this.imageUrl = reviewImage.getImageUrl();
    }
} 