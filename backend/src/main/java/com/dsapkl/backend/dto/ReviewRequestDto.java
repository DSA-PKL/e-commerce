package com.dsapkl.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

@Getter
@Builder
public class ReviewRequestDto {
    private Long itemId;

    @NotBlank(message = "Please enter the review content.")
    @Size(min = 1, max = 1000, message = "Review must be 1000 characters or less.")
    private String content;

    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating must be 5 or less.")
    private int rating;
} 