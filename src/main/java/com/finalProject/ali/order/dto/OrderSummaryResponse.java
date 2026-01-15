package com.finalProject.ali.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OrderSummaryResponse {

    private Long orderId;
    private Long totalAmount;
    private String status;
    private LocalDateTime createdAt;

    private String firstProductName;
    private String firstOptionSummary;
    private int itemCount;

    private String shipStatus;
}
