package com.finalProject.ali.supplier_mypage.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OptionDTO {
    private String optionId;
    private Long productId;
    private String optionName;
    private String optionValue;
}
