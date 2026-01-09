package com.finalProject.ali.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemResponse {
    private String skuId;
    private Long quantity;
    private Long unitPrice;
    private Long lineAmount;
    private String productName;
    private String optionSummary;

    private String status;
    private String carrier;
    private String trackingNo;

}
