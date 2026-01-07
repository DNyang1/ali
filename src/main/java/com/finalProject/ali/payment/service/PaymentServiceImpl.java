package com.finalProject.ali.payment.service;

import com.finalProject.ali.order.domain.Order;
import com.finalProject.ali.order.mapper.OrderMapper;
import com.finalProject.ali.payment.domain.Payment;
import com.finalProject.ali.payment.dto.PaymentRequest;
import com.finalProject.ali.payment.dto.PaymentResponse;
import com.finalProject.ali.payment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{
    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentResponse pay(PaymentRequest request) {

        Order order = orderMapper.findById(request.getOrderId());

        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setOrderId(order.getOrderId());
        payment.setPaymentMethod(request.getMethod());
        payment.setAmount(order.getTotalAmount());

        payment.setStatus("SUCCESS");

        paymentMapper.insert(payment);

        // + 오더상태 변경

        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setStatus(payment.getStatus());

        return response;
    }
}
