package com.dsapkl.backend.repository;

import com.dsapkl.backend.entity.Order;
import com.dsapkl.backend.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {
}
