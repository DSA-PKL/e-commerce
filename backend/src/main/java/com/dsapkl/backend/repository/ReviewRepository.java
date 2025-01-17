package com.dsapkl.backend.repository;

import com.dsapkl.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByItemIdAndMemberId(Long itemId, Long memberId);
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.item.id = :itemId")
    Optional<Double> findAverageRatingByItemId(@Param("itemId") Long itemId);
    @Query("SELECT r FROM Review r JOIN FETCH r.member WHERE r.item.id = :itemId ORDER BY r.createdDate DESC")
    List<Review> findByItemIdOrderByCreatedDateDesc(@Param("itemId") Long itemId);
    List<Review> findByMemberIdOrderByCreatedDateDesc(Long memberId);
} 