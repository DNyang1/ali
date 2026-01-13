package com.finalProject.ali.shipping.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class ShippingResponse {

    private String carrier;
    private String trackingNo;
    private TrackingEvent lastEvent;
    private List<TrackingEvent> trackingHistory;

    @Getter
    @Setter
    public static class TrackingEvent {
        private String time;
        private String location;
        private ShippingStatus status;
        private String description;
    }

    public static ShippingResponse unknown(String carrier, String trackingNo) {
        TrackingEvent unknownEvent = new TrackingEvent();
        unknownEvent.setTime(LocalDateTime.now().toString());
        unknownEvent.setLocation("");
        unknownEvent.setStatus(ShippingStatus.UNKNOWN);
        unknownEvent.setDescription("배송 정보 확인 불가");

        ShippingResponse response = new ShippingResponse();
        response.setCarrier(carrier);
        response.setTrackingNo(trackingNo);
        response.setLastEvent(unknownEvent);
        response.setTrackingHistory(Collections.singletonList(unknownEvent));
        return response;
    }
}
