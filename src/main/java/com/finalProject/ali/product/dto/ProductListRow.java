package com.finalProject.ali.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductListRow {
    private Long productId;
    private String productName;
    private Long isCustomizable;
    private String status;
}
