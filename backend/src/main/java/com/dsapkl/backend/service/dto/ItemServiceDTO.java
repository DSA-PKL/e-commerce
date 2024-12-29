package com.dsapkl.backend.service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Builder
public class ItemServiceDTO {

    private Long id;
    private String name;
    private int price;
    private int stockQuantity;
    private String description;
    private List<MultipartFile> itemImages;
}
