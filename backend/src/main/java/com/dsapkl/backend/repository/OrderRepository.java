package com.dsapkl.backend.repository;

import com.dsapkl.backend.entity.Order;
import com.dsapkl.backend.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {
    @Query("SELECT COUNT(o) FROM Order o WHERE o.member.id = :memberId")
    Integer findPurchaseFrequencyByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT AVG(o.totalPrice) FROM Order o WHERE o.member.id = :memberId")
    Integer findAverageOrderValueByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.member.id = :memberId")
    Integer findTotalSpendingByMemberId(@Param("memberId") Long memberId);
}
