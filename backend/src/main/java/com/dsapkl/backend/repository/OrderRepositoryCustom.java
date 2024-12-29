package com.dsapkl.backend.repository;

import com.dsapkl.backend.entity.OrderStatus;

import java.util.List;

public interface OrderRepositoryCustom {
    List<OrderDto> findOrderDtos(Long memberId, OrderStatus orderStatus);

    List<OrderDto> findOrderDetail(Long memberId, OrderStatus orderStatus);
}
