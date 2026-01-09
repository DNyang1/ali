package com.finalProject.ali.order.service;

import com.finalProject.ali.order.dto.*;

import java.util.List;

public interface OrderService {
    OrderCreateResponse createOrder(String userId, OrderCreateRequest request);
    List<OrderSummaryResponse> getMyOrders(String userId);
    OrderDetailResponse getOrderDetail(Long orderId, String userId);
    List<SupplierOrderItemResponse> getSupOrders(String supplierId);
}
