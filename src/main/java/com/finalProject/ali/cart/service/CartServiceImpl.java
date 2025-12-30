package com.finalProject.ali.cart.service;

import com.finalProject.ali.cart.domain.Cart;
import com.finalProject.ali.cart.domain.CartItem;
import com.finalProject.ali.cart.dto.AddCartItemRequest;
import com.finalProject.ali.cart.dto.CartItemResponse;
import com.finalProject.ali.cart.dto.CartResponse;
import com.finalProject.ali.cart.mapper.CartItemMapper;
import com.finalProject.ali.cart.mapper.CartMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService{

    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    @Override
    public CartResponse getCart(String userId) {
        Cart cart = cartMapper.findActiveCart(userId);

        if (cart == null) {
            cart = new Cart();
            cart.setCartId(UUID.randomUUID().toString());
            cart.setUserId(userId);
            cart.setStatus("ACTIVE");
            cartMapper.insert(cart);
        }

        List<CartItem> items = cartItemMapper.findByCartId(cart.getCartId());
        List<CartItemResponse> responses = new ArrayList<>();
        for (CartItem item : items) {
            CartItemResponse dto = new CartItemResponse();
            dto.setCartItemId(item.getCartItemId());
            dto.setQuantity(item.getQuantity());
            responses.add(dto);
        }

        CartResponse response = new CartResponse();
        response.setCartId(cart.getCartId());
        response.setItems(responses);
        return response;

    }

    @Override
    public void addItem(String userId, AddCartItemRequest request) {

        Cart cart = cartMapper.findActiveCart(userId);

        if (cart == null) {
            cart = new Cart();
            cart.setCartId(UUID.randomUUID().toString());
            cart.setUserId(userId);
            cart.setStatus("ACTIVE");
            cartMapper.insert(cart);
        }
        
        CartItem item =
                cartItemMapper.findByCartIdAndProductId(
                        cart.getCartId(),
                        request.getProductId()
                );

        if (item == null) {
            CartItem newItem = new CartItem();
            newItem.setCartItemId(UUID.randomUUID().toString());
            newItem.setCartId(cart.getCartId());
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            cartItemMapper.insert(newItem);
        } else {
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemMapper.updateQuantity(item);
        }
    }

    @Override
    public void deleteItem(String userId, String cartItemId) {

        Cart cart = cartMapper.findActiveCart(userId);
        if (cart == null) {
            return;
        }

        cartItemMapper.deleteById(cartItemId);
    }
}
