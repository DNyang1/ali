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

    private final ImageService imageService; // 기존 그대로
    private final ProductImageDAO dao;

    @Transactional
    public List<ProductImageDTO> uploadThumb(Long productId, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("대표 이미지를 선택하세요.");

        ProductImageDTO old = dao.findThumb(productId);
        if (old != null) {
            imageService.deleteActualFile(old.getImagePath());
            dao.deleteById(old.getImageId());
        }

        String path = imageService.uploadImage(file, "product/" + productId);
        dao.insert(productId, "THUMB", path, 0);

        return dao.findByType(productId, "THUMB");
    }

    @Transactional
    public List<ProductImageDTO> uploadDetails(Long productId, MultipartFile[] files) {
        if (files == null || files.length == 0) throw new IllegalArgumentException("상세 이미지를 선택하세요.");

        int order = (dao.nextSort(productId, "DETAIL") == null) ? 0 : dao.nextSort(productId, "DETAIL");

        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) continue;
            String path = imageService.uploadImage(f, "product/" + productId);
            dao.insert(productId, "DETAIL", path, order++);
        }

        return dao.findByType(productId, "DETAIL");
    }

    @Transactional(readOnly = true)
    public List<ProductImageDTO> list(Long productId, String type) {
        return dao.findByType(productId, type);
    }
    @Transactional
    public void deleteImage(Long imageId) {
        ProductImageDTO img = dao.findById(imageId);
        if (img == null) return;

        if (img.getImagePath() != null) {
            imageService.deleteActualFile(img.getImagePath());
        }
        dao.deleteById(imageId);
    }
}
