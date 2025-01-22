package com.dsapkl.backend.service;

import com.dsapkl.backend.entity.*;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import com.dsapkl.backend.repository.MemberInfoRepository;
import com.dsapkl.backend.repository.ClusterItemPreferenceRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final FileHandler filehandler;
    private final ItemImageService itemImageService;
    private final MemberInfoRepository memberInfoRepository;
    private final ClusterItemPreferenceRepository clusterItemPreferenceRepository;

    //상품 정보 저장
    public Long saveItem(ItemForm itemForm, List<MultipartFile> multipartFileList, Long memberId) throws IOException {
        Item item = Item.createItem(
                itemForm.getName(),
                itemForm.getPrice(),
                itemForm.getStockQuantity(),
                itemForm.getDescription(),
                itemForm.getCategory(),
                memberId);

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

    @Transactional(readOnly = true)
    public Page<Item> searchItems(String query, String category, String status, Pageable pageable) {
        Specification<Item> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 검색어 조건
            if (query != null && !query.trim().isEmpty()) {
                String pattern = "%" + query.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
                ));
            }
            
            // 카테고리 조건
            if (category != null && !category.trim().isEmpty()) {
                try {
                    Category categoryEnum = Category.valueOf(category.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("category"), categoryEnum));
                } catch (IllegalArgumentException e) {
                    log.debug("Invalid category value: {}", category);
                    // 잘못된 카테고리는 무시하고 계속 진행
                }
            }
            
            return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
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

    public void checkItemOwner(Long itemId, Long memberId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
        
        if (!item.getMemberId().equals(memberId)) {
            throw new IllegalStateException("해당 상품의 수정/삭제 권한이 없습니다.");
        }
    }

    public List<Item> findLatestItems(int limit) {
        return itemRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                            .stream()
                            .limit(limit)
                            .collect(Collectors.toList());
    }

    public List<Item> getRecommendedItems(Long memberId) {
        // 회원의 클러스터 정보 조회
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElse(null);
        
        if (memberInfo == null || memberInfo.getCluster_id() == null) {
            return Collections.emptyList();
        }

        // 해당 클러스터의 선호도 높은 아이템 조회 (최대 4개)
        return clusterItemPreferenceRepository.findByClusterIdOrderByPreferenceScoreDesc(memberInfo.getCluster_id().getId())
                .stream()
                .map(ClusterItemPreference::getItem)
                .limit(4)
                .collect(Collectors.toList());
    }
}
