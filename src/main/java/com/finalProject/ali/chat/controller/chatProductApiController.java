package com.finalProject.ali.chat.controller;

import com.finalProject.ali.chat.dto.ProductSummaryDTO;
import com.finalProject.ali.products.dto.ProductsDTO;
import com.finalProject.ali.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class chatProductApiController {

    private final ProductService productService;

    @GetMapping("/{productId}/summary")
    public ProductSummaryDTO getSummary(@PathVariable Long productId) {
        ProductsDTO p = productService.productDetail(productId);
        if (p == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        ProductSummaryDTO dto = new ProductSummaryDTO();
        dto.setProductId(p.getProductId());
        dto.setProductName(p.getProductName());

        // 이미지 구조 아직 없으면 null
        dto.setThumbnailUrl(null);

        return dto;
    }
}
