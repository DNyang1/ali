package com.finalProject.ali.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SkuPriceDTO {
    //공통
    private Long skuPriceId;
    private String skuId;
    private Long minQty;
    private Long maxQty;
    private Long unitPrice;

    //태민
    private LocalDateTime createdAt;
}
