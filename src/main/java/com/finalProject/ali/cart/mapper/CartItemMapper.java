package com.finalProject.ali.cart.mapper;

import com.finalProject.ali.cart.domain.CartItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CartItemMapper {
    List<CartItem> findByCartId(String cartId);
    CartItem findByCartIdAndProductId(String cartId, String productId, String skuId);
    void insert(CartItem item);
    void updateQuantity(CartItem item);
    void deleteById(String cartItemId);
}
