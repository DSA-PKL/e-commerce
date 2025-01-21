package com.dsapkl.backend.dto;

import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.entity.Category;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemForm {

    private Long itemId;

    @NotEmpty(message = "Product name is required.")
    private String name;

    @NotNull(message = "Category information is required.")
    private Category category;

    @NotNull(message = "Product price is required.")
    private int price;

    @NotNull(message = "Product stock quantity is required.")
    private int stockQuantity;

    private String description;

    private Long memberId;

    private List<ItemImageDto> itemImageListDto = new ArrayList<>();

    private double averageRating;

    private int reviewCount;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

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
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setDescription(item.getDescription());
        form.setCategory(item.getCategory());
        form.setMemberId(item.getMemberId());
        form.setCreatedDate(item.getCreatedDate());
        form.setModifiedDate(item.getModifiedDate());
        return form;
    }

    public ItemServiceDTO toServiceDTO() {
        return ItemServiceDTO.builder()
                .itemId(itemId)
                .name(name)
                .category(category)
                .price(price)
                .stockQuantity(stockQuantity)
                .description(description)
                .build();
    }

}
