package com.finalProject.ali.mypage.supplier.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SkuPriceDTO {
    private Long skuPriceId;
    private String skuId;
    private Long minQty;
    private Long maxQty;
    private Long price;
    private LocalDateTime createdAt;
}
