package com.finalProject.ali.order.dto;

import com.finalProject.ali.checkout.dto.CheckoutItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderCreateRequest {
    private Long addressId;
    private Long totalAmount;
    private List<CheckoutItem> items;
}
