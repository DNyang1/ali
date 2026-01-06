package com.finalProject.ali.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DirectOrderRequest {
    private Long productId;
    private String skuId;
    private Long quantity;
}
