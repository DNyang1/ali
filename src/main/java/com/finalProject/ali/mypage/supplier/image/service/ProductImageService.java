package com.finalProject.ali.mypage.supplier.image.service;

import com.finalProject.ali.image.service.ImageService;
import com.finalProject.ali.mypage.supplier.image.dao.ProductImageDAO;
import com.finalProject.ali.mypage.supplier.image.dto.ProductImageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductImageDAO dao;
    private final ImageService imageService;

    @Transactional
    public List<ProductImageDTO> uploadThumb(Long productId, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("대표 이미지 파일이 비어있음");
        }

        ProductImageDTO old = dao.findThumb(productId);
        if (old != null) {
            imageService.deleteActualFile(old.getImagePath());
            dao.deleteById(old.getImageId());
        }

        String savedPath = imageService.uploadImage(file, "product");
        if (savedPath == null || savedPath.isBlank()) {
            throw new IllegalStateException("대표 이미지 저장 실패(ImageService 반환 null)");
        }

        int sortOrder = 1;

        dao.insert(productId, "THUMB", savedPath, sortOrder);

        return dao.findByType(productId, "THUMB");
    }

    @Transactional
    public List<ProductImageDTO> uploadDetails(Long productId, MultipartFile[] files) {

        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("상세 이미지 파일이 비어있음");
        }

        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) continue;

            String savedPath = imageService.uploadImage(f, "product");
            if (savedPath == null || savedPath.isBlank()) {
                throw new IllegalStateException("상세 이미지 저장 실패(ImageService 반환 null)");
            }

            Integer next = dao.nextSort(productId, "DETAIL");
            int sortOrder = (next == null ? 1 : next);

            dao.insert(productId, "DETAIL", savedPath, sortOrder);
        }

        return dao.findByType(productId, "DETAIL");
    }

    public List<ProductImageDTO> list(Long productId, String type) {
        return dao.findByType(productId, type);
    }

    @Transactional
    public void deleteImage(Long imageId) {
        ProductImageDTO img = dao.findById(imageId);
        if (img == null) return;

        imageService.deleteActualFile(img.getImagePath());
        dao.deleteById(imageId);
    }
}

