package com.finalProject.ali.order.service;

import com.finalProject.ali.order.dto.OrderCreateResponse;

public interface OrderService {
    OrderCreateResponse createOrder(String userId);
}
