package com.finalProject.ali.payment.service;

import com.finalProject.ali.order.domain.Order;
import com.finalProject.ali.order.mapper.OrderMapper;
import com.finalProject.ali.payment.dto.PaymentRequest;
import com.finalProject.ali.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{
    private final OrderMapper orderMapper;

    @Override
    public PaymentResponse pay(PaymentRequest request) {

        Order order = orderMapper.findById(request.getOrderId());





        return null;
    }
}
