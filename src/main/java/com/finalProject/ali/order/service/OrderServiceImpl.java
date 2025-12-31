package com.finalProject.ali.order.service;

import com.finalProject.ali.cart.dto.CartItemResponse;
import com.finalProject.ali.cart.dto.CartResponse;
import com.finalProject.ali.cart.service.CartService;
import com.finalProject.ali.order.domain.Order;
import com.finalProject.ali.order.domain.OrderItem;
import com.finalProject.ali.order.dto.OrderCreateResponse;
import com.finalProject.ali.order.mapper.OrderItemMapper;
import com.finalProject.ali.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private final CartService cartService;
//    private final SkuPriceMapper skuPriceMapper;
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

        cartService.clearCart(userId);

        OrderCreateResponse res = new OrderCreateResponse();
        res.setOrderId(order.getOrderId());
        res.setTotalAmount(total);
        return res;
    }
}
