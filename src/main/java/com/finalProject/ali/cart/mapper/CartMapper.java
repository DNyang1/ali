package com.finalProject.ali.cart.mapper;

import com.finalProject.ali.cart.domain.Cart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper {

    Cart findActiveCart(String userId);
    void insert(Cart cart);
    int countCartItemByUser(String userId);
}
