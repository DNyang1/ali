package com.finalProject.ali.product.dto;

import lombok.Data;

@Data
public class CustomOrderSheetForm {
    private Long inquiryId;
    private Long quantity;
    private Long unitPrice;
    private String optionsText;
}
