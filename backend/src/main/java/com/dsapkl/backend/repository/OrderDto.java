package com.dsapkl.backend.repository;

import com.dsapkl.backend.entity.OrderStatus;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

@Data
@EqualsAndHashCode(of = "orderId")
public class OrderDto {
    private Long orderId;
    private int totalPrice;  //총 주문 가격
    private String orderDate;
    private OrderStatus orderStatus;
    private List<OrderItemDto> orderItemDtoList;


    @QueryProjection
    public OrderDto(Long orderId, int totalPrice, LocalDateTime orderDate, OrderStatus orderStatus) {
        this.orderId = orderId;
        this.totalPrice = totalPrice;
//        this.orderDate = orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));  //보기 좋게 format
        this.orderDate = orderDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));  //보기 좋게 format
        this.orderStatus = orderStatus;
    }
}
