package com.dsapkl.backend.repository;

import lombok.Data;

@Data
public class OrderItemDto {
    private String itemName;  //주문 상품 이름
    private int orderPrice; //주문 상품 가격
    private int count; //주문 수량
    private String imgUrl; //대표 상품 이미지 경로

    public OrderItemDto(String itemName, int orderPrice, int count, String imgUrl) {
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.count = count;
        this.imgUrl = imgUrl;
    }
}
