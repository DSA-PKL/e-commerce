package com.dsapkl.backend.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartForm {

    private Long itemId;
    private Long cartItemId;
    private int count;
    private String paymentIntentId;

    // 기본 생성자 추가
    public CartForm() {
    }
//
//    public CartForm(Long itemId, Long cartItemId, int count, String paymentIntentId) {
//        this.itemId = itemId;
//        this.count = count;
//        this.paymentIntentId = paymentIntentId;
//    }

    public CartForm(Long itemId, int count, String paymentIntentId) {
        this.itemId = itemId;
        this.count = count;
        this.paymentIntentId = paymentIntentId;
    }

    public CartForm(Long itemId, Long cartItemId, int count) {
        this.itemId = itemId;
        this.cartItemId = cartItemId;
        this.count = count;
    }

    public CartForm(Long itemId,  int count) {
        this.itemId = itemId;
        this.count = count;
    }


    public CartForm(Long cartItemId, Long itemId, int count, String paymentIntentId) {
        this.cartItemId = cartItemId;
        this.itemId = itemId;
        this.count = count;
        this.paymentIntentId = paymentIntentId;
    }
}
