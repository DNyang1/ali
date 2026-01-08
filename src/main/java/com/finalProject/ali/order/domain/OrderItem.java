package com.finalProject.ali.order.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OrderItem {
    private Long orderItemId;
    private Long orderId;
    private String skuId;
    private Long quantity;
    private Long unitPrice;
    private LocalDateTime createdAt;

    private String productName;
    private String optionSummary;
}
