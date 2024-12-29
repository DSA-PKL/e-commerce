package com.dsapkl.backend.repository;

import com.dsapkl.backend.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {

    List<ItemImage> findByItemIdAndDeleteYN(Long itemId, String deleteYN);

    List<ItemImage> findAllByDeleteYN(String deleteYN);
}
