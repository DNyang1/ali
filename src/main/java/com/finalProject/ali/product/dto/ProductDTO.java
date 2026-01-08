package com.finalProject.ali.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class ProductDTO {
    // 공통
    private Long productId;
    private String categoryId;
    private String supplierId;
    private String productName;
    private String description;
    private Long isCustomizable;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    // 태민
    private String status;
    public boolean isCustomizable() {
        return this.isCustomizable == 1;
    }
    // 현성
    private Long moq;
    private Long minPrice;
    private Long maxPrice;
}
