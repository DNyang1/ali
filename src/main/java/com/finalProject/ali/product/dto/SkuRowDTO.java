package com.finalProject.ali.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkuRowDTO {
    //공통
    private String skuId;
    private Integer stockQuantity;
    //현성
    private Long productId;
    private String optionValueId;
    //태민
    private Long moq;
    private String status;
}
