package com.finalProject.ali.cart.service;

import com.finalProject.ali.cart.dto.AddCartItem;
import com.finalProject.ali.cart.dto.CartDTO;

public interface CartService {
    CartDTO getCart(String userId);
    void addItem(String userId, AddCartItem request);
}
