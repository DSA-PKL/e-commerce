package com.dsapkl.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {  // 생성/수정 시간 추적을 위한 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int rating;  // 1-5 평점

    @Builder
    public Review(Item item, Member member, String content, int rating) {
        this.item = item;
        this.member = member;
        this.content = content;
        this.rating = rating;
    }

    // 리뷰 수정 메서드
    public void update(String content, int rating) {
        this.content = content;
        this.rating = rating;
    }
} 