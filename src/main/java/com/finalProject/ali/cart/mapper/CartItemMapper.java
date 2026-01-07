package com.finalProject.ali.cart.mapper;

import com.finalProject.ali.cart.domain.CartItem;
import com.finalProject.ali.cart.dto.CartItemViewResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CartItemMapper {
    List<CartItem> findByCartId(String cartId);
    CartItem findByCartIdAndProductIdAndSkuId(String cartId, Long productId, String skuId);
    void insert(CartItem item);
    void updateQuantity(CartItem item);
    void deleteById(String cartItemId);
    List<CartItemViewResponse> findCartItemViews(String cartId);
}
