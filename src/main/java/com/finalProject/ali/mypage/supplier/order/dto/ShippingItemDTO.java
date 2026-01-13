package com.finalProject.ali.mypage.supplier.order.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShippingItemDTO {
    private Long orderItemId;
    private Long orderId;
    private String skuId;

    private Long quantity;
    private Long unitPrice;

    private String productName;
    private String optionSummary;

    private String supplierStatus; // READY / SHIPPED ...
    private String carrier;
    private String trackingNo;

    private LocalDateTime createdAt;
}
