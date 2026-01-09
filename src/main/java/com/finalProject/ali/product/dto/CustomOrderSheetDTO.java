package com.finalProject.ali.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomOrderSheetDTO {
    private Long sheetId;
    private Long productId;
    private Long inquiryId;
    private String skuId;
    private String status;

    private Long quantity;
    private Long unitPrice;
    private String optionsText;
}
