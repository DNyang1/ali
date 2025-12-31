package com.finalProject.ali.cart.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItem {
    private String cartItemId;
    private String cartId;
    private String productId;
    private Long quantity;
}
