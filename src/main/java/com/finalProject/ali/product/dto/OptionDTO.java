package com.finalProject.ali.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OptionDTO {

    //공통
    private String optionName;
    private String optionValue;

    //태민
    private String optionId;
    private Long productId;
    private String status;

    // 현성
    private String optionValueId;
}
