package com.finalProject.ali.mypage.supplier.image.controller;

import com.finalProject.ali.mypage.supplier.image.dto.ProductImageDTO;
import com.finalProject.ali.mypage.supplier.image.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage/supplier/product")
public class ProductImageController {

    private final ProductImageService service;

    @PostMapping("/{productId}/images/thumb")
    public ResponseEntity<List<ProductImageDTO>> uploadThumb(
            @PathVariable Long productId,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(service.uploadThumb(productId, file));
    }

    @PostMapping("/{productId}/images/detail")
    public ResponseEntity<List<ProductImageDTO>> uploadDetail(
            @PathVariable Long productId,
            @RequestPart("files") MultipartFile[] files
    ) {
        return ResponseEntity.ok(service.uploadDetails(productId, files));
    }

    @GetMapping("/{productId}/images")
    public ResponseEntity<List<ProductImageDTO>> list(
            @PathVariable Long productId,
            @RequestParam String type
    ) {
        return ResponseEntity.ok(service.list(productId, type));
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        service.deleteImage(imageId);
        return ResponseEntity.ok().build();
    }
}
