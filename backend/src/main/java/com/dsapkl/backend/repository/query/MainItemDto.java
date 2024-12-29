package com.dsapkl.backend.repository.query;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MainItemDto {

    private Long itemId;
    private String itemName;
    private int itemPrice;
    private String imgUrl;

    @QueryProjection
    public MainItemDto(Long itemId, String itemName, int itemPrice, String imgUrl) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.imgUrl = imgUrl;
    }

}
