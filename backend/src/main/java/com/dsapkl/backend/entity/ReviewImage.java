package com.dsapkl.backend.entity;

import com.dsapkl.backend.entity.BaseTimeEntity;
import com.dsapkl.backend.entity.Review;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @Column(name = "original_name")
    private String originalFileName;

    @Column(name = "store_name")
    private String storeFileName;

    @Column(name = "deleteyn")
    private String deleteYN = "N";

    @Builder
    public ReviewImage(String originalFileName, String storeFileName) {
        this.originalFileName = originalFileName;
        this.storeFileName = storeFileName;
        this.deleteYN = "N";
    }

    public void setReview(Review review) {
        this.review = review;
    }

    public void delete() {
        this.deleteYN = "Y";
    }

    public String getImageUrl() {
        return "/images/" + this.getStoreFileName();
    }
} 