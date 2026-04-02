package com.finalProject.ali.payment.service;


import com.finalProject.ali.order.domain.Order;
import com.finalProject.ali.order.mapper.OrderMapper;
import com.finalProject.ali.payment.domain.Payment;
import com.finalProject.ali.payment.dto.PaymentRequest;
import com.finalProject.ali.payment.dto.PaymentResponse;
import com.finalProject.ali.payment.mapper.PaymentMapper;
//태민
import com.finalProject.ali.product.dao.CustomOrderSheetDAO;
import com.finalProject.ali.inquiry.dao.InquiryDAO;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{
    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    //태민
    private final CustomOrderSheetDAO customOrderSheetDAO;
    private final InquiryDAO inquiryDAO;

    @Override
    @Transactional // 태민 sql 3개를 동시에 묶음으로써 오류 방지
    public PaymentResponse pay(PaymentRequest request) {

        Order order = orderMapper.findById(request.getOrderId());
        if (order == null) {
            throw new IllegalArgumentException("주문 정보를 찾을 수 없습니다.");
        }

        // 1. 주문 상태 가드: 이미 결제되었거나 취소된 주문인지 확인
        if (!"CREATED".equals(order.getStatus())) {
            throw new IllegalStateException("결제 가능한 상태가 아닙니다. (현재 상태: " + order.getStatus() + ")");
        }

        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setOrderId(order.getOrderId());
        payment.setPaymentMethod(request.getMethod());
        payment.setAmount(order.getTotalAmount());

        payment.setStatus("SUCCESS");

        paymentMapper.insert(payment);

        orderMapper.updateStatus(order.getOrderId(), "PAID");
        //태민
        customOrderSheetDAO.markPaidByOrderId(order.getOrderId());
        Long inquiryId = customOrderSheetDAO.findInquiryIdByOrderId(order.getOrderId());
        if (inquiryId != null) {
            inquiryDAO.updateStatus(inquiryId, 2L);
        }


        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setStatus(payment.getStatus());

        return response;
    }
}
