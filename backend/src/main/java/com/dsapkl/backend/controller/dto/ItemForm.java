package com.dsapkl.backend.controller.dto;

import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.entity.Category;
import com.dsapkl.backend.service.dto.ItemServiceDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemForm {

    private Long itemId;

    @NotEmpty(message = "상품 이름은 필수입니다.")
    private String name;

    @NotNull(message = "카테고리 정보 입력 필수")
    private Category category;

    @NotNull(message = "상품 가격은 필수입니다.")
    private int price;

    @NotNull(message = "상품 재고 수량은 필수입니다.")
    private int stockQuantity;

    private String description;

    private List<ItemImageDto> itemImageListDto = new ArrayList<>();

    private double averageRating;

    private int reviewCount;

    @Builder
    public ItemForm(String name, int price, int stockQuantity, String description) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.description = description;
    }

    public static ItemForm from(Item item) {
        ItemForm form = new ItemForm();
        form.setItemId(item.getId());
        form.setName(item.getName());
        form.setCategory(item.getCategory());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setDescription(item.getDescription());
        form.setAverageRating(item.getAverageRating());
        form.setReviewCount(item.getReviewCount());
        return form;
    }

    public ItemServiceDTO toServiceDTO() {
        return ItemServiceDTO.builder()
                .id(itemId)
                .name(name)
                .category(category)
                .price(price)
                .stockQuantity(stockQuantity)
                .description(description)
                .build();
    }

}
