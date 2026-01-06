package com.finalProject.ali.cart.controller;

import com.finalProject.ali.cart.dto.AddCartItemRequest;
import com.finalProject.ali.cart.dto.CartResponse;
import com.finalProject.ali.cart.service.CartService;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        String userId = user.getUserId();
        return cartService.getCart(userId);
    }

    @PostMapping("/items")
    public void addItem(
            @RequestBody AddCartItemRequest request, HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        String userId = user.getUserId();
        cartService.addItem(userId, request);
    }

    @DeleteMapping("/items/{cartItemId}")
    public void deleteItem(@PathVariable String cartItemId, HttpSession session){
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        String userId = user.getUserId();
        cartService.deleteItem(userId, cartItemId);
    }
}
