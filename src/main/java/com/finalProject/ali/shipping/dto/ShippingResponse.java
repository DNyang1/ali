package com.finalProject.ali.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ShippingResponse {

    private String carrier;
    private String trackingNo;
    private ShippingStatus status;
    private String message;
    private LocalDateTime checkedAt;

    public static ShippingResponse unknown(String carrier, String trackingNo) {
        return new ShippingResponse(
                carrier,
                trackingNo,
                ShippingStatus.UNKNOWN,
                "배송 정보 확인 불가",
                LocalDateTime.now()
        );
    }

}
