package com.finalProject.ali.mypage.supplier.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class ProductDTO {
    private Long productId;
    private String categoryId;
    private String supplierId;
    private String productName;
    private String description;
    private Long isCustomizable;
    private Long moq;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private String status;
}
