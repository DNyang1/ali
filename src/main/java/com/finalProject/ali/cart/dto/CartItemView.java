package com.finalProject.ali.cart.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemView {
    private String cartItemId;
    private Long productId;
    private String productName;
    private String skuId;
    private Long quantity;

    private String optionSummary;
}
