package com.dsapkl.backend.controller.guest;


import com.dsapkl.backend.dto.ItemForm;
import com.dsapkl.backend.dto.ItemImageDto;
import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.entity.ItemImage;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.ItemImageService;
import com.dsapkl.backend.service.ItemService;
import com.dsapkl.backend.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class GuestItemController {

    private final ItemService itemService;
    private final ItemImageService itemImageService;
    private final CartService cartService;

    /**
     * 상품 상세 조회
     */
    @GetMapping("items/{itemId}")
    public String itemView(@PathVariable(name = "itemId") Long itemId, Model model, HttpServletRequest request) {
        Item item = itemService.findItem(itemId);
        if (item == null) {
            return "redirect:/";
        }

        List<ItemImage> itemImageList = itemImageService.findItemImageDetail(itemId, "N");
        List<ItemImageDto> itemImageDtoList = itemImageList.stream()
                .map(ItemImageDto::new)
                .collect(Collectors.toList());

        ItemForm itemForm = ItemForm.from(item);
        itemForm.setItemImageListDto(itemImageDtoList);
        model.addAttribute("item", itemForm);

        // 카트 숫자 // th:text="${cartItemCount}" 쓰기 위함


        Member member = SessionUtil.getMember(request);
        if (member != null) {
            List<CartQueryDto> cartItemListForm = cartService.findCartItems(member.getId());
            int cartItemCount = cartItemListForm.size();
            model.addAttribute("cartItemListForm", cartItemListForm);
            model.addAttribute("cartItemCount", cartItemCount);
        }

        model.addAttribute("currentMemberId", member != null ? member.getId() : null);

        return "item/itemView";
    }

}
