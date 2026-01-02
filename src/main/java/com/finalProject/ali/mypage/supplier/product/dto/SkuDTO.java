package com.finalProject.ali.mypage.supplier.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SkuDTO {
    private String skuId;
    private Long productId;
    private Long stockQuantity;
    private LocalDate createdAt;

    private String optionSummary;
    private BigDecimal basePrice;
    private boolean editable;
}
