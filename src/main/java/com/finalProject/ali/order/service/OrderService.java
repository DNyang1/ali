package com.finalProject.ali.order.service;

import com.finalProject.ali.order.domain.OrderPreviewResponse;
import com.finalProject.ali.order.dto.OrderCreateResponse;

public interface OrderService {
    OrderCreateResponse createOrder(String userId);
    OrderPreviewResponse getOrderPreview(String userId);

}
