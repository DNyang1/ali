package com.finalProject.ali.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchSummaryDTO {
    // 🔹 식별자
    private Long productId;
    private Long categoryId;

    // 🔹 표시 정보
    private String name;
    private String categoryName;
    private boolean custom;
    private String priceRange;


}
