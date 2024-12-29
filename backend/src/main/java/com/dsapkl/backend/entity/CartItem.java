package com.dsapkl.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    @Id
    @Column(name = "cart_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int count;  //장바구니 아이템 수량

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private CartItem(int count, Cart cart, Item item) {
        this.count = count;
        this.cart = cart;
        this.item = item;
    }

    public static CartItem createCartItem(int count, Cart cart, Item item) {
        return new CartItem(count, cart, item);
    }

    public void changeCount(int count) {
        this.count = count;
    }
}
