package com.dsapkl.backend.controller;

import com.dsapkl.backend.controller.dto.ItemForm;
import com.dsapkl.backend.controller.dto.ItemImageDto;
import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.entity.ItemImage;
import com.dsapkl.backend.service.ItemImageService;
import com.dsapkl.backend.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
//@Slf4j
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final ItemImageService itemImageService;

    @GetMapping("/items/new")
    public String createItemForm(Model model) {
        model.addAttribute("itemForm", new ItemForm());
        return "item/itemForm";
    }

    @PostMapping("/items/new")
    public String createItem(@Valid @ModelAttribute ItemForm itemForm, BindingResult bindingResult, Model model,
                             @RequestPart(name = "itemImages") List<MultipartFile> multipartFiles
    ) throws IOException {

        if (bindingResult.hasErrors()) return "item/itemForm";

        //상품 이미지를 등록안하면
        if (multipartFiles.get(0).isEmpty()) {
            model.addAttribute("errorMessage", "상품 사진을 등록해주세요!");
            return "item/itemForm";
        }

        //

        itemService.saveItem(itemForm.toServiceDTO(), multipartFiles);

        return "redirect:/";
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("items/{itemId}")
    public String itemView(@PathVariable(name = "itemId") Long itemId, Model model) {
        Item item = itemService.findItem(itemId);
        List<ItemImage> itemImageList = itemImageService.findItemImageDetail(itemId, "N");

        //엔티티 -> DTO
        List<ItemImageDto> itemImageDtoList = itemImageList.stream()
                .map(ItemImageDto::new)
                .collect(Collectors.toList());

        ItemForm itemform = new ItemForm(
                item.getId(),
                item.getName(),
                item.getPrice(),
                item.getStockQuantity(),
                item.getDescription(),
                itemImageDtoList
        );

        model.addAttribute("item", itemform);

        return "item/itemView";

    }


}
