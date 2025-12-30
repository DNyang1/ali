package com.finalProject.ali.cart.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddCartItemRequest {
    private String productId;
    private Long quantity;
    private String skuId;
}
