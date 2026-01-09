package com.finalProject.ali.checkout.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutItem {
    private Long productId;
    private String productName;
    private String optionSummary;
    private String skuId;
    private Long quantity;
    private Long unitPrice;
    private Long lineAmount;
}
