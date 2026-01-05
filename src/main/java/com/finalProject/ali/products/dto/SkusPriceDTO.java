package com.finalProject.ali.products.dto;

import lombok.Data;


@Data
public class SkusPriceDTO {
    private Long skuPriceId;
    private String skuId;

    private Long minQty;
    private Integer maxQty;
    private Long unitPrice;
}
