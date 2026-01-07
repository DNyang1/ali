package com.finalProject.ali.order.service;

import com.finalProject.ali.cart.dto.CartItemResponse;
import com.finalProject.ali.cart.dto.CartResponse;
import com.finalProject.ali.cart.service.CartService;
import com.finalProject.ali.order.domain.Order;
import com.finalProject.ali.order.domain.OrderItem;
import com.finalProject.ali.order.dto.OrderCreateRequest;
import com.finalProject.ali.order.dto.OrderCreateResponse;
import com.finalProject.ali.order.mapper.OrderItemMapper;
import com.finalProject.ali.order.mapper.OrderMapper;
import com.finalProject.ali.product.dao.SkuPriceDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private final CartService cartService;
    private final SkuPriceDAO skuPriceDAO;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public OrderCreateResponse createOrder(String userId, OrderCreateRequest request) {

        Order order = new Order();
        order.setUserId(userId);
        order.setAddressId(request.getAddressId());
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus("CREATED");
        orderMapper.insert(order);

        request.getItems().forEach(item -> {
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getOrderId());
            oi.setSkuId(item.getSkuId());
            oi.setQuantity(item.getQuantity());
            oi.setUnitPrice(item.getUnitPrice());

            orderItemMapper.insert(oi);
        });


        OrderCreateResponse res = new OrderCreateResponse();
        res.setOrderId(order.getOrderId());
        res.setTotalAmount(order.getTotalAmount());
        return res;
    }

}
