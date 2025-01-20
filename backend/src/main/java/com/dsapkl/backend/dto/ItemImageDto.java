package com.dsapkl.backend.dto;

import com.dsapkl.backend.entity.ItemImage;
import lombok.Getter;
import lombok.Setter;
import lombok.Data;

@Getter
@Setter
public class ItemImageDto {

    private Long id;
    private String originalName;
    private String storeName;
    private String deleteYN;


    public ItemImageDto(ItemImage itemImage) {
        this.id = itemImage.getId();
        this.originalName = itemImage.getOriginalName();
        this.storeName = itemImage.getStoreName();
        this.deleteYN = itemImage.getDeleteYN();
    }

    public String getImageUrl() {
        if (storeName == null) return null;
        if (storeName.startsWith("http")) {
            return storeName;
        }
        return "/images/" + storeName;
    }
}
