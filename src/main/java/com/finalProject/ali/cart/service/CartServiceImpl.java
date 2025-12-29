package com.finalProject.ali.cart.service;

import com.finalProject.ali.cart.dto.AddCartItem;
import com.finalProject.ali.cart.dto.CartDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService{

    @Override
    public CartDTO getCart(String userId) {
        return null;
    }

    @Override
    public void addItem(String userId, AddCartItem request) {

    }
}
