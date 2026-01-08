package com.finalProject.ali.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OrderSummaryResponse {
    // TODO 무엇을 결제했는가? 를위한거 하나 만들어야함,
    //  : order에 상품정보 간단 스냅샷 저장할수있는 컬럼필요
    private Long orderId;
    private Long totalAmount;
    private String status;
    private LocalDateTime createAd;
}
