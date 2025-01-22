package com.dsapkl.backend.controller;

import com.dsapkl.backend.dto.ItemForm;
import com.dsapkl.backend.dto.ItemImageDto;
import com.dsapkl.backend.dto.ItemStats;
import com.dsapkl.backend.entity.Category;
import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.entity.ItemImage;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.repository.MemberRepository;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.ItemImageService;
import com.dsapkl.backend.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dsapkl.backend.controller.CartController.getMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
//@Slf4j
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final ItemImageService itemImageService;
    private final CartService cartService;
    private static final Logger log = LoggerFactory.getLogger(ItemController.class);
    private final MemberRepository memberRepository;

    @Value("${stripe.public.key}")
    private String stripePublicKey;

//    APPAREL, ELECTRONICS, BOOKS, HOME_AND_KITCHEN, HEALTH_AND_BEAUTY

    @GetMapping("/items/new")
    public String createItemForm(Model model) {
        List<CategoryCode> categoryCode = new ArrayList<>();
        categoryCode.add(new CategoryCode("APPAREL", "Apparel"));
        categoryCode.add(new CategoryCode("ELECTRONICS", "Electronics"));
        categoryCode.add(new CategoryCode("BOOKS", "Books"));
        categoryCode.add(new CategoryCode("HOME_AND_KITCHEN", "Home & Kitchen"));
        categoryCode.add(new CategoryCode("HEALTH_AND_BEAUTY", "Health & Beauty"));
        model.addAttribute("categoryCode", categoryCode);
        model.addAttribute("itemForm", new ItemForm());
        return "item/itemForm";
    }

    @Data
    @AllArgsConstructor
    static class CategoryCode {
        private String code;
        private String displayName;
    }

    @PostMapping("/items/new")
    public String createItem(@Valid ItemForm itemForm, BindingResult result, Model model,
                           @RequestParam("itemImages") List<MultipartFile> itemImages,
                           HttpServletRequest request) {
        Member member = getMember(request);
        if (member == null) {
            return "redirect:/members/login";
        }
        
        try {
            itemService.saveItem(itemForm, itemImages, member.getId());
        } catch (IOException e) {
            model.addAttribute("errorMessage", "An error occurred while registering the product.");
            return "item/itemForm";
        }
        
        return "redirect:/";
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("/items/{itemId}")
    public String itemView(@PathVariable("itemId") Long itemId, Model model, HttpServletRequest request) {
        try {
            ItemForm itemForm = itemService.getItemDtl(itemId);
            if (itemForm == null) {
                return "redirect:/";
            }

            Member member = getMember(request);
            
            // 장바구니 아이템 카운트
            if (member != null) {
                List<CartQueryDto> cartItems = cartService.findCartItems(member.getId());
                model.addAttribute("cartItemCount", cartItems.size());
            }
            
            model.addAttribute("item", itemForm);
            model.addAttribute("currentMemberId", member != null ? member.getId() : null);
            model.addAttribute("stripePublicKey", stripePublicKey);
            
            return "item/itemView";
        } catch (Exception e) {
            log.error("Error in itemView: ", e);
            return "redirect:/";
        }
    }

    /**
     * 상품 삭제
     */
    @PostMapping("/items/{itemId}/delete")
    @ResponseBody
    public ResponseEntity<String> deleteItem(@PathVariable Long itemId, HttpServletRequest request) {
        Member member = getMember(request);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to log in.");
        }
        
        try {
            itemService.checkItemOwner(itemId, member.getId());  // 권한 체크
            itemService.deleteItem(itemId);
            return ResponseEntity.ok("The product has been deleted.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/items/{itemId}/edit")
    public String updateItem(@PathVariable("itemId") Long itemId,
                             @RequestParam("name") String name,
                             @RequestParam("price") int price,
                             @RequestParam("stockQuantity") int stockQuantity,
                             @RequestParam("description") String description,
                             @RequestParam("category") Category category,
                             @RequestParam(value = "deleteImages", required = false) List<Long> deleteImageIds,
                             @RequestParam(value = "itemImages", required = false) List<MultipartFile> itemImages,
                             HttpServletRequest request) throws IOException {
        // 상품 수정 로직
        itemService.updateItem(itemId, name, price, stockQuantity, description, category);

        // 선택된 이미지 삭제
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            for (Long imageId : deleteImageIds) {
                itemImageService.delete(imageId);
            }
        }

        // 이미지가 있다면 이미지도 수정
        if (itemImages != null && !itemImages.isEmpty() && !itemImages.get(0).isEmpty()) {
            itemImageService.updateItemImages(itemId, itemImages);
        }

        // Referer 헤더를 통해 이전 페이지 URL 확인
        String referer = request.getHeader("Referer");
        // itemForm.html에서 온 요청인지 확인 (상품 관리 페이지에서의 수정)
        if (referer != null && referer.contains("/items/" + itemId + "/edit")) {
            return "redirect:/items/manage";
        } else {
            // itemView.html에서의 수정
            return "redirect:/items/" + itemId;
        }
    }

    @GetMapping("/api/items/{itemId}/rating")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getItemRating(@PathVariable Long itemId) {
        Item item = itemService.findItem(itemId);
        Map<String, Object> response = new HashMap<>();
        response.put("averageRating", item.getAverageRating());
        response.put("reviewCount", item.getReviewCount());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/items/manage")
    public String manageItems(Model model, 
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String query,
                             @RequestParam(required = false) String category,
                             @RequestParam(required = false) String status,
                             HttpServletRequest request) {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Member member = getMember(request);
            if (member == null) {
                return "redirect:/members/login";
            }
            
            // 현재 사용자 ID를 모델에 추가
            model.addAttribute("currentMemberId", member.getId());
            
            // 장바구니 아이템 카운트 추가
            List<CartQueryDto> cartItems = cartService.findCartItems(member.getId());
            model.addAttribute("cartItemCount", cartItems.size());
            
            // 전체 아이템 조회 (통계용)
            List<Item> allItems = itemService.findAll();
            
            // ItemStats 생성
            ItemStats itemStats = ItemStats.builder()
                    .totalCount(itemService.count())
                    .lowStockCount(allItems.stream()
                            .filter(item -> item.getStockQuantity() <= 10 && item.getStockQuantity() > 0)
                            .count())
                    .onSaleCount(allItems.stream()
                            .filter(item -> item.getStockQuantity() > 0)
                            .count())
                    .soldOutCount(allItems.stream()
                            .filter(item -> item.getStockQuantity() == 0)
                            .count())
                    .build();
            
            // 검색 파라미터 유지를 위한 모델 속성 추가
            if (category != null && !category.trim().isEmpty()) {
                try {
                    Category selectedCategory = Category.valueOf(category.toUpperCase());
                    model.addAttribute("selectedCategory", selectedCategory);
                } catch (IllegalArgumentException e) {
                    // 잘못된 카테고리 값은 무시
                }
            }
            
            if (status != null && !status.trim().isEmpty()) {
                model.addAttribute("selectedStatus", status);
            }
            
            // 페이지네이션된 데이터 조회
            Page<Item> itemPage;
            if (query != null || category != null || status != null) {
                itemPage = itemService.searchItems(query, category, status, PageRequest.of(page, size));
            } else {
                itemPage = itemService.findItemsPage(PageRequest.of(page, size));
            }
            
            // ItemForm으로 변환
            List<ItemForm> itemForms = itemPage.getContent().stream()
                .map(item -> {
                    ItemForm form = ItemForm.from(item);
                    List<ItemImage> images = itemImageService.findItemImageDetail(item.getId(), "N");
                    form.setItemImageListDto(images.stream()
                        .map(ItemImageDto::new)
                        .collect(Collectors.toList()));
                    return form;
                })
                .collect(Collectors.toList());
                
            model.addAttribute("items", itemForms);
            model.addAttribute("itemStats", itemStats);
            model.addAttribute("currentPage", itemPage.getNumber());
            model.addAttribute("totalPages", itemPage.getTotalPages());
            
            return "item/itemManage";
        } catch (Exception e) {
            log.error("Error in manageItems: ", e);
            return "redirect:/";
        }
    }

    @GetMapping("/items/{itemId}/edit")
    public String itemEditForm(@PathVariable("itemId") Long itemId, Model model, HttpServletRequest request) {
        Member member = getMember(request);
        if (member == null) {
            return "redirect:/members/login";
        }
        
        try {
            itemService.checkItemOwner(itemId, member.getId());  // 권한 체크
            ItemForm itemForm = itemService.getItemDtl(itemId);
            List<CategoryCode> categoryCode = new ArrayList<>();
            categoryCode.add(new CategoryCode("APPAREL", "Apparel"));
            categoryCode.add(new CategoryCode("ELECTRONICS", "Electronics"));
            categoryCode.add(new CategoryCode("BOOKS", "Books"));
            categoryCode.add(new CategoryCode("HOME_AND_KITCHEN", "Home & Kitchen"));
            categoryCode.add(new CategoryCode("HEALTH_AND_BEAUTY", "Health & Beauty"));
            model.addAttribute("itemForm", itemForm);
            model.addAttribute("categoryCode", categoryCode);
            model.addAttribute("isEdit", true);  // 수정 모드임을 표시
            return "item/itemForm";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/items/manage";
        }
    }

}
