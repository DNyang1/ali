package com.finalProject.ali.order.service;

import com.finalProject.ali.order.domain.Order;
import com.finalProject.ali.order.domain.OrderItem;
import com.finalProject.ali.order.dto.*;
import com.finalProject.ali.order.mapper.OrderItemMapper;
import com.finalProject.ali.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

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
            oi.setProductName(item.getProductName());
            oi.setOptionSummary(item.getOptionSummary());

            orderItemMapper.insert(oi);
        });


        OrderCreateResponse res = new OrderCreateResponse();
        res.setOrderId(order.getOrderId());
        res.setTotalAmount(order.getTotalAmount());
        return res;
    }

    @Override
    public List<OrderSummaryResponse> getMyOrders(String userId) {
        return orderMapper.findMyOrders(userId);
    }

    @Override
    public OrderDetailResponse getOrderDetail(Long orderId, String userId) {

        Order order = orderMapper.findByIdAndUser(orderId, userId);

        if (order == null) {
            throw new IllegalArgumentException("주문을 찾을수 없음");
        }

        List<OrderItemResponse> items = orderItemMapper.findByOrderId(orderId);

        OrderDetailResponse res = new OrderDetailResponse();
        res.setOrderId(order.getOrderId());
        res.setStatus(order.getStatus());
        res.setTotalAmount(order.getTotalAmount());
        res.setCreatedAt(order.getCreatedAt().toString());
        res.setItems(items);

        return res;
    }

    @Override
    public List<SupplierOrderItemResponse> getSupOrders(String supplierId) {
        return orderMapper.findSupplierOrderItems(supplierId);
    }


}
