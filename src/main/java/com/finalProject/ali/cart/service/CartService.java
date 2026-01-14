package com.finalProject.ali.cart.service;

import com.finalProject.ali.cart.dto.AddCartItemRequest;
import com.finalProject.ali.cart.dto.CartResponse;
import com.finalProject.ali.cart.dto.CartViewResponse;

import java.util.List;

public interface CartService {
    CartResponse getCart(String userId);
    void addItem(String userId, List<AddCartItemRequest> request);

    void deleteItem(String userId, String cartItemId);
    CartViewResponse getCartView(String userId);
    int getCartItemCount(String userId);
}
