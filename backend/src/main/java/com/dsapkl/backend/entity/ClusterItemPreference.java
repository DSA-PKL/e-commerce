package com.dsapkl.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cluster_item_preference")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClusterItemPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cluster_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_number")
    private Cluster cluster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "preference_score")
    private Integer preferenceScore;

    // 생성자
    public ClusterItemPreference(Cluster cluster, Item item) {
        this.cluster = cluster;
        this.item = item;
        this.preferenceScore = 0;
    }

    // 선호도 점수 증가 메서드
    public void increasePreferenceScore() {
        this.preferenceScore += 1;
    }

    // 선호도 점수 설정 메서드
    public void setPreferenceScore(Integer preferenceScore) {
        this.preferenceScore = preferenceScore;
    }
}