package com.finalProject.ali.cart.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartItemResponse {
    private String cartItemId;
    private Long productId;
    private String skuId;
    private Long quantity;
}
