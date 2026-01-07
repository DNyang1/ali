package com.finalProject.ali.cart.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CartViewResponse {
    private String CartId;
    private List<CartItemViewResponse> items;
}
