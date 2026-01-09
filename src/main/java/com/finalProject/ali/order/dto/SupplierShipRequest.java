package com.finalProject.ali.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierShipRequest {
    private String carrier;
    private String trackingNo;
}
