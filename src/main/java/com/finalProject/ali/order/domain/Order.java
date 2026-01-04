package com.finalProject.ali.order.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class Order {
    private Long orderId;
    private String userId;
    private Long totalAmount;
    private LocalDateTime createdAt;
}
