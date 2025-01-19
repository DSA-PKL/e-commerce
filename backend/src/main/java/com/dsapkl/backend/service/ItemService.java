package com.dsapkl.backend.service;

import com.dsapkl.backend.entity.Category;
import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.entity.ItemImage;
import com.dsapkl.backend.repository.ItemImageRepository;
import com.dsapkl.backend.repository.ItemRepository;
import com.dsapkl.backend.dto.ItemServiceDTO;
import com.dsapkl.backend.dto.ItemForm;
import com.dsapkl.backend.dto.ItemImageDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final FileHandler filehandler;
    private final ItemImageService itemImageService;

    //상품 정보 저장
    public Long saveItem(ItemServiceDTO itemServiceDTO, List<MultipartFile> multipartFileList) throws IOException {
        Item item = Item.createItem(
                itemServiceDTO.getName(),
                itemServiceDTO.getPrice(),
                itemServiceDTO.getStockQuantity(),
                itemServiceDTO.getDescription(),
                itemServiceDTO.getCategory());

        List<ItemImage> itemImages = filehandler.storeImages(multipartFileList);

        //대표 상품 이미지 설정

        if (!itemImages.isEmpty()) {
            itemImages.get(0).isFirstImage("Y");  // 첫 번째 이미지를 대표 이미지로 설정
        }

        for (ItemImage itemImage : itemImages) {
            item.addItemImage(itemImage);  // 연관 관계 설정
        }

        itemRepository.save(item);

        return item.getId();
    }

    //상품 정보 업데이트 (Dirty Checking, 변경감지)
    @Transactional
    public void updateItem(ItemServiceDTO itemServiceDTO, List<MultipartFile> multipartFileList) throws IOException {
        Item findItem = itemRepository.findById(itemServiceDTO.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Product does not exist."));

        findItem.updateItem(
                itemServiceDTO.getName(),
                itemServiceDTO.getPrice(),
                itemServiceDTO.getStockQuantity(),
                itemServiceDTO.getDescription(),
                itemServiceDTO.getCategory()
        );

        //상품 이미지를 수정(삭제, 추가) 하지 않으면 실행 x
        if(!multipartFileList.get(0).isEmpty()) {
            itemImageService.addItemImage(multipartFileList, findItem);
        }

        //대표 이미지 재설정
        List<ItemImage> itemImageList = itemImageRepository.findByItemIdAndDeleteYN(itemServiceDTO.getItemId(), "N");
        itemImageList.get(0).isFirstImage("Y");
    }

    @Transactional
    public void updateItem(Long itemId, String name, int price, int stockQuantity, String description, Category category) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("The product does not exist."));

        item.updateItem(name, price, stockQuantity, description, category);
    }

    // 통합 검색 기능
    @Transactional(readOnly = true)
    public List<Item> searchItems(String name, String categoryStr) {
        Category category = null;
        if (categoryStr != null && !categoryStr.isEmpty()) {
            try {
                category = Category.valueOf(categoryStr);
            } catch (IllegalArgumentException e) {
                // 잘못된 카테고리 문자열이 들어온 경우 전체 상품 반환
                return itemRepository.findAll();
            }
        }

        if (name != null && !name.isEmpty()) {
            if (category != null) {
                return itemRepository.findByCategoryAndNameContainingIgnoreCase(category, name);
            }
            return itemRepository.findByNameContainingIgnoreCase(name);
        }

        if (category != null) {
            return itemRepository.findByCategory(category);
        }

        return itemRepository.findAll();
    }

    @Transactional(readOnly=true)
    public List<Item> findItemsPaging() {
        Page<Item> result = itemRepository.findAll(PageRequest.of(0, 3));
        return result.getContent();
    }


    @Transactional(readOnly = true)
    public Item findItem(Long ItemId) {

        return itemRepository.findById(ItemId).orElse(null);
    }

    /**
     * 상품 삭제
     */
    public void deleteItem(Long itemId) {
        // 삭제 전 존재 여부 확인
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found with ID: " + itemId));

        // 관련 이미지 삭제 처리
        List<ItemImage> itemImages = itemImageRepository.findByItemIdAndDeleteYN(itemId,"N");

        for (ItemImage image : itemImages) {
            filehandler.deleteImage(image.getStoreFileName()); // 실제 파일 삭제 (FileHandler 활용)
            itemImageRepository.delete(image); // DB에서 이미지 삭제
        }

        // 상품 삭제
        itemRepository.delete(item);
    }

    public Page<Item> findItemsPage(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }

    public Page<Item> searchItems(String query, String categoryStr, String status, Pageable pageable) {
        final Category selectedCategory = (categoryStr != null && !categoryStr.isEmpty()) ? 
            Category.valueOf(categoryStr) : null;

        Specification<Item> spec = Specification.where(null);
        
        if (selectedCategory != null) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("category"), selectedCategory)
            );
        }

        if (query != null && !query.isEmpty()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + query.toLowerCase() + "%"),
                    criteriaBuilder.like(root.get("id").as(String.class), "%" + query + "%")
                )
            );
        }

        if (status != null) {
            switch (status) {
                case "SELLING":
                    spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                        criteriaBuilder.greaterThan(root.get("stockQuantity"), 10));
                    break;
                case "LOW_STOCK":
                    spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                        criteriaBuilder.and(
                            criteriaBuilder.greaterThan(root.get("stockQuantity"), 0),
                            criteriaBuilder.lessThanOrEqualTo(root.get("stockQuantity"), 10)
                        ));
                    break;
                case "SOLDOUT":
                    spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("stockQuantity"), 0));
                    break;
            }
        }

        return itemRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public ItemForm getItemDtl(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);
        ItemForm itemForm = ItemForm.from(item);
        
        List<ItemImage> itemImages = itemImageRepository.findByItemIdAndDeleteYN(itemId, "N");
        List<ItemImageDto> itemImageDtos = itemImages.stream()
                .map(ItemImageDto::new)
                .collect(Collectors.toList());
        
        itemForm.setItemImageListDto(itemImageDtos);
        return itemForm;
    }

    public long count() {
        return itemRepository.count();  // JPA Repository의 기본 count() 메서드 사용
    }

    @Transactional(readOnly = true)
    public List<Item> findAll() {
        return itemRepository.findAll();
    }
}
