package com.dsapkl.backend.repository;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDto {
    private Long itemId;  // 추가
    private String itemName;  //주문 상품 이름
    private int orderPrice; //주문 상품 가격
    private int count; //주문 수량
    private String imgUrl; //대표 상품 이미지 경로

    public OrderItemDto(Long itemId, String itemName, int count, int orderPrice, String imgUrl) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.count = count;
        this.orderPrice = orderPrice;
        this.imgUrl = imgUrl;
    }

    public OrderItemDto(String itemName, int count, int orderPrice, String imgUrl) {
        this.itemName = itemName;
        this.count = count;
        this.orderPrice = orderPrice;
        this.imgUrl = imgUrl;
    }
}
