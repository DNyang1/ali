package com.finalProject.ali.checkout.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CheckoutResponse {
    private List<CheckoutItem> items;
    private Long totalAmount;
    private Long addressId;
}
