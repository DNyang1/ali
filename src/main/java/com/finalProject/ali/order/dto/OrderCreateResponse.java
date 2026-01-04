package com.finalProject.ali.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCreateResponse {
    private Long orderId;
    private Long totalAmount;
}
