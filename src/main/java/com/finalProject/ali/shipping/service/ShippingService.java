package com.finalProject.ali.shipping.service;

import com.finalProject.ali.order.domain.OrderItem;
import com.finalProject.ali.order.dto.OrderItemResponse;
import com.finalProject.ali.order.mapper.OrderItemMapper;
import com.finalProject.ali.shipping.client.ShippingClient;
import com.finalProject.ali.shipping.dto.ShippingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingService {

    private final OrderItemMapper orderItemMapper;
    private final ShippingClient shippingClient;

    public ShippingResponse getShipping(Long orderItemId) {

        OrderItem item = orderItemMapper.findById(orderItemId);

        if (item == null) {
            throw new IllegalArgumentException("주문 상품이 없습니다.");
        }

        if (item.getCarrier() == null || item.getTrackingNo() == null) {
            throw new IllegalStateException("송장 정보가 아직 등록되지 않았습니다.");
        }

        return shippingClient.check(item.getCarrier(), item.getTrackingNo());
    }

    public List<String> getCarrierNames() {
        return ShippingClient.getSupportedCarriers();
    }
}
