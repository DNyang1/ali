package com.finalProject.ali.mypage.user.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserRecentOrderResponse {
    private Long orderId;
    private String orderNo;
    private String status;
    private LocalDateTime createdAt;
    private Long totalAmount;
}
