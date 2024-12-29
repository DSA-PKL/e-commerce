package com.dsapkl.backend.controller.dto;

import com.dsapkl.backend.entity.Item;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemDto {

    private Long id;
    private String name;
    private String description;
    private int price;
    private int stockQuantity;

    public ItemDto(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.price = item.getPrice();
        this.stockQuantity = item.getStockQuantity();
    }
}
