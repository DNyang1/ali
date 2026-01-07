package com.finalProject.ali.order.service;

import com.finalProject.ali.order.domain.OrderPreviewResponse;
import com.finalProject.ali.order.dto.OrderCreateRequest;
import com.finalProject.ali.order.dto.OrderCreateResponse;

public interface OrderService {
    OrderCreateResponse createOrder(String userId, OrderCreateRequest request);
    OrderPreviewResponse getOrderPreview(String userId);
    OrderPreviewResponse getDirectOrderPreview(Long productId, String skuId, Long quantity);
}
