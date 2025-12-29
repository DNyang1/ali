package com.finalProject.ali.cart.controller;

import com.finalProject.ali.cart.dto.AddCartItem;
import com.finalProject.ali.cart.dto.CartDTO;
import com.finalProject.ali.cart.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private String getCurrentUserId(HttpSession session) {
        return (String) session.getAttribute("loginUser");
    }

    @GetMapping
    public CartDTO getCart(HttpSession session) {
        String userId = getCurrentUserId(session);
        return cartService.getCart(userId);
    }

    public void addItem(@RequestBody AddCartItem request, HttpSession session) {
        String userId = getCurrentUserId(session);
        cartService.addItem(userId, request);
    }
}
