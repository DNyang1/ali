package com.finalProject.ali.shipping.client;

import com.finalProject.ali.shipping.dto.ShippingResponse;
import com.finalProject.ali.shipping.dto.ShippingStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ShippingClient {

    public ShippingResponse check(String carrier, String trackingNo) {

        char last = trackingNo.charAt(trackingNo.length() - 1);

        if (last == '1') {
            return new ShippingResponse(
                    carrier,
                    trackingNo,
                    ShippingStatus.READY,
                    "배송 준비 중",
                    LocalDateTime.now()
            );
        }

        if (last == '5') {
            return new ShippingResponse(
                    carrier,
                    trackingNo,
                    ShippingStatus.SHIPPING,
                    "배송 중",
                    LocalDateTime.now()
            );
        }

        if (last == '9') {
            return new ShippingResponse(
                    carrier,
                    trackingNo,
                    ShippingStatus.DONE,
                    "배송 완료",
                    LocalDateTime.now()
            );
        }

        return ShippingResponse.unknown(carrier, trackingNo);
    }
}

