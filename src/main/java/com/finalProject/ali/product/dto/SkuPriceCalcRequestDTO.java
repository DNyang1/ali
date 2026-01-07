package com.finalProject.ali.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SkuPriceCalcRequestDTO {
    private String skuId;
    private Long qty;
}
