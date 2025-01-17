package com.dsapkl.backend.service;

import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.entity.ItemImage;
import com.dsapkl.backend.repository.ItemImageRepository;
import com.dsapkl.backend.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemImageService {

    private final ItemImageRepository itemImageRepository;
    private final FileHandler fileHandler;
    private final ItemRepository itemRepository;

    //삭제 여부를 판단하여 상품 이미지 정보를 조회한다
    @Transactional(readOnly = true)
    public List<ItemImage> findItemImageDetail(Long itemId, String YN) {
        return itemImageRepository.findByItemIdAndDeleteYN(itemId, YN);
    }

    @Transactional(readOnly = true)
    public List<ItemImage> findAllByDeleteYN(String YN) {
        return itemImageRepository.findAllByDeleteYN(YN);
    }

    //데이터베이스를 삭제하지 않고 flag를 이용하여 처리
    public void delete(Long itemImageId) {
        ItemImage itemImage = itemImageRepository.findById(itemImageId).get();
        itemImage.deleteSet("Y");

        //대표 상품 이미지 삭제
        itemImage.isFirstImage("N");
    }

    //상품 이미지 추가
    public void addItemImage(List<MultipartFile> multipartFiles, Item item) throws IOException {
        List<ItemImage> itemImages = fileHandler.storeImages(multipartFiles);

        for (ItemImage itemImage : itemImages) {
            item.addItemImage(itemImageRepository.save(itemImage));
        }
    }

    @Transactional
    public void updateItemImages(Long itemId, List<MultipartFile> itemImageFiles) throws IOException {
        // 기존 이미지 삭제
        List<ItemImage> existingImages = itemImageRepository.findByItemIdOrderByIdAsc(itemId);
        for (ItemImage image : existingImages) {
            fileHandler.deleteImage(image.getStoreName());
            itemImageRepository.delete(image);
        }

        // 새 이미지 저장
        List<ItemImage> newImages = fileHandler.storeImages(itemImageFiles);
        Item item = itemRepository.findById(itemId).orElseThrow();
        for (int i = 0; i < newImages.size(); i++) {
            ItemImage image = newImages.get(i);
            image.changeItem(item);
            image.setRepImgYn(i == 0 ? "Y" : "N");
            itemImageRepository.save(image);
        }
    }

    @Transactional(readOnly = true)
    public List<ItemImage> findByItemIdAndRepImgYn(Long itemId, String repImgYn) {
        return itemImageRepository.findByItemIdAndRepImgYn(itemId, repImgYn);
    }
}
