package com.finalProject.ali.shipping.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SweetTrackerApiResponse {
    private Company company;
    private String invoiceNo;
    private String itemName;
    private String receiverName;
    private String receiverAddr;
    private String senderName;
    private List<TrackingDetail> trackingDetails;
    private LastDetail lastDetail;
    private String result; // "Y" for success, "N" for failure
    private String shippingStatus; // "inProgress", "completed", "ready" etc.

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class Company {
        private String id;
        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class TrackingDetail {
        private String kind;
        private Integer level;
        private Location location;
        private String telno;
        private String time;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class Location {
        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class LastDetail {
        private String kind;
        private Integer level;
        private Location location;
        private String telno;
        private String time;
    }
}
