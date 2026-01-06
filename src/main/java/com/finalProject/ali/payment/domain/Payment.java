package com.finalProject.ali.payment.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Payment {

    private String paymentId;
    private Long orderId;
    private String paymentMethod;
    private String status;
    private Long amount;
}
