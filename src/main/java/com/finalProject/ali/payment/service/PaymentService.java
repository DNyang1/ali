package com.finalProject.ali.payment.service;

import com.finalProject.ali.payment.dto.PaymentRequest;
import com.finalProject.ali.payment.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse pay(PaymentRequest request);
}
