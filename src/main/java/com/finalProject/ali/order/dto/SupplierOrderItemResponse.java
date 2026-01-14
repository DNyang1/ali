package com.finalProject.ali.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SupplierOrderItemResponse {
    private Long orderItemId;
    private Long orderId;
    private LocalDateTime orderedAt;
    private String buyerId;
    private String productName;
    private String optionSummary;
    private Long quantity;
    private Long unitPrice;
    private String orderStatus;
    private String supplierStatus;
}
