package com.dsapkl.backend.service;

import com.dsapkl.backend.entity.ItemImage;
import com.dsapkl.backend.entity.ReviewImage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FileHandler {

    @Value("${file.dir}")
    private String fileDir;  // "C:/pkl/images/"

    //파일 경로명
    public String getFullPath(String filename) {
        return fileDir + filename;
    }


    private String createStoreImageName(String oriImageName) {
        String ext = extractExt(oriImageName);  //jpeg
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    //파일 경로명(스토리지)에 사진 저장
    public List<ItemImage> storeImages(List<MultipartFile> multipartFiles) throws IOException {
        List<ItemImage> storeResult = new ArrayList<>();

        for (MultipartFile multipartfile : multipartFiles) {
            storeResult.add(storeImage(multipartfile));
        }
        return storeResult;
    }

    public ItemImage storeImage(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }

        // ex) image1.jpeg
        String oriImageName = multipartFile.getOriginalFilename();

        //서버에 저장될 파일명
        String storeImageName = createStoreImageName(oriImageName);

        //스토리지에 저장
        multipartFile.transferTo(new File(getFullPath(storeImageName)));

        return ItemImage.builder()
                .originalName(oriImageName)
                .storeName(storeImageName)
                .build();
    }


    //확장자 추출
    private String extractExt(String oriImageName) {
        int pos = oriImageName.lastIndexOf(".");
        return oriImageName.substring(pos + 1);
    }

    public void deleteImage(String fileName) {
        String filePath = fileDir + fileName;
        File file = new File(filePath);

        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                throw new RuntimeException("Failed to delete file: " + filePath);
            }
        }
    }

    public List<ReviewImage> storeFiles(List<MultipartFile> files) throws IOException {
        List<ReviewImage> reviewImages = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                String storeFileName = createStoreImageName(originalFilename);
                file.transferTo(new File(getFullPath(storeFileName)));
                reviewImages.add(ReviewImage.builder()
                        .originalFileName(originalFilename)
                        .storeFileName(storeFileName)
                        .build());
            }
        }
        return reviewImages;
    }

    public List<ReviewImage> parseFileInfo(List<MultipartFile> multipartFiles) throws Exception {
        List<ReviewImage> reviewImages = new ArrayList<>();
        
        File dir = new File(fileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        for (MultipartFile file : multipartFiles) {
            if (!file.isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                String storeFileName = createStoreImageName(originalFilename);
                
                // 파일 저장
                file.transferTo(new File(getFullPath(storeFileName)));
                
                // ReviewImage 객체 생성 및 추가
                ReviewImage reviewImage = ReviewImage.builder()
                    .originalFileName(originalFilename)
                    .storeFileName(storeFileName)
                    .build();
                    
                reviewImages.add(reviewImage);
            }
        }
        
        return reviewImages;
    }
}
