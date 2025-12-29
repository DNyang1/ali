package com.finalProject.ali.cart.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddCartItem {
    private String productId;
    private Long quantity;
}
