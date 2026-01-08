package com.finalProject.ali.order.service;

import com.finalProject.ali.order.dto.OrderCreateRequest;
import com.finalProject.ali.order.dto.OrderCreateResponse;
import com.finalProject.ali.order.dto.OrderDetailResponse;
import com.finalProject.ali.order.dto.OrderSummaryResponse;

import java.util.List;

public interface OrderService {
    OrderCreateResponse createOrder(String userId, OrderCreateRequest request);
    List<OrderSummaryResponse> getMyOrders(String userId);
    OrderDetailResponse getOrderDetail(Long orderId, String userId);

}
