package com.dsapkl.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @Column(name = "order_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int count;
    private int orderPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private OrderItem(int count, int orderPrice, Item item) {
        this.count = count;
        this.orderPrice = orderPrice;
        this.item = item;
    }

    public void changeOrder(Order order) {
        this.order = order;
    }

    public static OrderItem createOrderItem(int count, int orderPrice, Item item) {
        item.minStock(count);
        return new OrderItem(count, orderPrice, item);
    }

    //== 비즈니스 메서드 ==//
    public void cancel() {
        item.addStock(count);
    }


}
