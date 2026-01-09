package com.finalProject.ali.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderDetailResponse {
    private Long orderId;
    private String status;
    private Long totalAmount;
    private String createdAt;
    private List<OrderItemResponse> items;

//    private Object payment;
//    private Object shipping;

}
