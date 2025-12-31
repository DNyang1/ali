package com.finalProject.ali.cart.service;

import com.finalProject.ali.cart.dto.AddCartItemRequest;
import com.finalProject.ali.cart.dto.CartResponse;

public interface CartService {
    CartResponse getCart(String userId);
    void addItem(String userId, AddCartItemRequest request);
}
