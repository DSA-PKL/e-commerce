package com.dsapkl.backend.entity;

import com.dsapkl.backend.exception.NotEnoughStockException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id
    @Column(name = "item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int price;
    private int stockQuantity;
    private String description;

    @Enumerated(value = EnumType.STRING)
    private Category category;

    @OneToMany(mappedBy = "item", cascade = CascadeType.PERSIST)
    private List<ItemImage> itemImageList = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.REMOVE)
    private List<Review> reviews = new ArrayList<>();

    //== 연관 관계 편의 메소드 ==//
    public void addItemImage(ItemImage itemImage) {
        itemImageList.add(itemImage);
        itemImage.changeItem(this);
    }


    @Builder
    private Item(String name, int price, int stockQuantity, String description, Category category) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.description = description;
        this.category = category;
    }



    public static Item createItem(String name, int price, int stockQuantity, String description, Category category) {
        return new Item(name, price, stockQuantity, description, category);
    }

       public void updateItem(String name, int price, int stockQuantity, String description, Category category) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.description = description;
        this.category = category;
    }

    //== 비즈니스 메서드 ==//

//    주문시 상품 재고 감소
    public void minStock(int quantity) {
        int restQuantity = stockQuantity - quantity;
        if (restQuantity < 0) {
            throw new NotEnoughStockException("상품 재고가 부족합니다!!");
        }
        stockQuantity = restQuantity;
    }

    //주문 취소시 상품 재고 증가
    public void addStock(int quantity) {
        stockQuantity += quantity;
    }

    // 평균 평점 계산 메서드
    public double getAverageRating() {
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    // 리뷰 수 조회 메서드
    public int getReviewCount() {
        return reviews.size();
    }

}
