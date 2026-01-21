package com.finalProject.ali.cart.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemViewResponse {
    private String cartItemId;
    private Long productId;
    private String productName;
    private String skuId;
    private Long quantity;
    private Long unitPrice;
    private Long lineAmount;
    private String thumbPath;

    private String optionSummary;
}
