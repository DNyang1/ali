package com.finalProject.ali.checkout.service;

import com.finalProject.ali.checkout.dto.CheckoutRequest;
import com.finalProject.ali.checkout.dto.CheckoutResponse;


public interface CheckoutService {
    CheckoutResponse checkout(String userId, CheckoutRequest request);
}
