package com.finalProject.ali.order.service;

import com.finalProject.ali.cart.dto.CartItemResponse;
import com.finalProject.ali.cart.dto.CartResponse;
import com.finalProject.ali.cart.service.CartService;
import com.finalProject.ali.order.domain.Order;
import com.finalProject.ali.order.domain.OrderItem;
import com.finalProject.ali.order.domain.OrderPreviewItem;
import com.finalProject.ali.order.domain.OrderPreviewResponse;
import com.finalProject.ali.order.dto.OrderCreateResponse;
import com.finalProject.ali.order.mapper.OrderItemMapper;
import com.finalProject.ali.order.mapper.OrderMapper;
import com.finalProject.ali.sku.mapper.SkuPriceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private final CartService cartService;
    private final SkuPriceMapper skuPriceMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public OrderCreateResponse createOrder(String userId) {

        CartResponse cart = cartService.getCart(userId);

        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(0L);
        orderMapper.insert(order);

        long total = 0L;

        for (CartItemResponse ci : cart.getItems()) {
            Long unitPrice = skuPriceMapper.findUnitPriceByQty(
                    ci.getSkuId(), ci.getQuantity()
            );
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getOrderId());
            oi.setSkuId(ci.getSkuId());
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(unitPrice);

            orderItemMapper.insert(oi);
            total += unitPrice * ci.getQuantity();
        }

        orderMapper.updateTotalAmount(order.getOrderId(), total);

        OrderCreateResponse res = new OrderCreateResponse();
        res.setOrderId(order.getOrderId());
        res.setTotalAmount(total);
        return res;
    }

    @Override
    public OrderPreviewResponse getOrderPreview(String userId) {

        CartResponse cart = cartService.getCart(userId);

        if (cart.getItems().isEmpty()) {
            return new OrderPreviewResponse(List.of(), 0L);
        }

        long total = 0L;
        List<OrderPreviewItem> result = new ArrayList<>();

        for (CartItemResponse ci : cart.getItems()) {
            Long unitPrice = skuPriceMapper.findUnitPriceByQty(
                    ci.getSkuId(), ci.getQuantity()
            );
            String productName = "test";
            long lineAmount = unitPrice * ci.getQuantity();

            OrderPreviewItem item = new OrderPreviewItem();
            item.setSkuId(ci.getSkuId());
            item.setProductName(productName);
            item.setProductId(ci.getProductId());
            item.setQuantity(ci.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setLineAmount(lineAmount);

            result.add(item);
            total += lineAmount;
        }


        return new OrderPreviewResponse(result,total);
    }
}
