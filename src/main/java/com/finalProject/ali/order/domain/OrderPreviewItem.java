package com.finalProject.ali.order.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderPreviewItem {
    private String skuId;
    private Long productId;
    private String productName;
    private Long quantity;
    private Long unitPrice;
    private Long lineAmount;
}
