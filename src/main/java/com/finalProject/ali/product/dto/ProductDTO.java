package com.finalProject.ali.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private String thumbPath;

    // 현성
    private Long moq;
    private Long minPrice;
    private Long maxPrice;
    private String thumbnail; // 나중에 img로 변경

    // 이벤트
    private String discountType;
    private Long discountValue;
    private LocalDateTime eventEndAt;

    public boolean isTimeSale() {
        return discountType != null
                && discountValue != null
                && eventEndAt != null
                && eventEndAt.isAfter(LocalDateTime.now());
    }
}
