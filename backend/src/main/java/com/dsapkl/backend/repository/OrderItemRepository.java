package com.dsapkl.backend.repository;

import com.dsapkl.backend.entity.Category;
import com.dsapkl.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi.item.category " +
           "FROM OrderItem oi " +
           "WHERE oi.order.member.id = :memberId " +
           "GROUP BY oi.item.category " +
           "ORDER BY COUNT(oi) DESC " +
           "LIMIT 1")
    Category findMostFrequentCategoryByMemberId(@Param("memberId") Long memberId);
}
