package com.finalProject.ali.products.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkuRowDTO {

    private String skuId;
    private Long productId;
    private Integer stockQuantity;
    private String optionValueId;
}
