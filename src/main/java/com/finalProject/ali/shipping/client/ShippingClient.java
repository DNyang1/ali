package com.finalProject.ali.shipping.client;

import com.finalProject.ali.shipping.dto.ShippingResponse;
import com.finalProject.ali.shipping.dto.ShippingStatus;
import com.finalProject.ali.shipping.dto.SweetTrackerApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections; // 추가
import java.util.stream.Collectors;

@Component
public class ShippingClient {

    private final RestTemplate restTemplate;
    private final String apiKey;

    private static final String SWEETTRACKER_API_URL = "http://info.sweettracker.co.kr/api/v1/trackingInfo";
    private static final Map<String, String> CARRIER_CODE_MAP;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("CJ대한통운", "04");
        map.put("한진택배", "05");
        map.put("롯데택배", "08");
        map.put("우체국택배", "01");
        map.put("로젠택배", "06");
        map.put("일양로지스", "11");
        map.put("경동택배", "23");
        map.put("대신택배", "22");
        map.put("CVSnet 편의점택배", "24");
        CARRIER_CODE_MAP = Collections.unmodifiableMap(map);
    }

    public ShippingClient(RestTemplate restTemplate, @Value("${shipping.tracker.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    public ShippingResponse check(String carrier, String trackingNo) {
        String tCode = CARRIER_CODE_MAP.get(carrier);
        if (tCode == null) {
            return ShippingResponse.unknown(carrier, trackingNo);
        }

        String url = UriComponentsBuilder.fromUriString(SWEETTRACKER_API_URL)
                .queryParam("t_key", apiKey)
                .queryParam("t_code", tCode)
                .queryParam("t_invoice", trackingNo)
                .build()
                .toUriString();

        try {
            SweetTrackerApiResponse response = restTemplate.getForObject(url, SweetTrackerApiResponse.class);

            if (response == null || !"Y".equals(response.getResult())) {
                return ShippingResponse.unknown(carrier, trackingNo);
            }

            return convertToShippingResponse(response);

        } catch (Exception e) {
            return ShippingResponse.unknown(carrier, trackingNo);
        }
    }

    private ShippingResponse convertToShippingResponse(SweetTrackerApiResponse sweetResponse) {
        if (sweetResponse.getTrackingDetails() == null || sweetResponse.getTrackingDetails().isEmpty()) {
            return ShippingResponse.unknown(sweetResponse.getCompany().getName(), sweetResponse.getInvoiceNo());
        }

        List<ShippingResponse.TrackingEvent> history = sweetResponse.getTrackingDetails().stream()
                .map(detail -> {
                    ShippingResponse.TrackingEvent event = new ShippingResponse.TrackingEvent();
                    event.setTime(detail.getTime());
                    event.setLocation(detail.getLocation().getName());
                    event.setStatus(mapLevelToStatus(detail.getLevel()));
                    event.setDescription(detail.getKind());
                    return event;
                })
                .collect(Collectors.toList());

        ShippingResponse.TrackingEvent lastEvent = history.get(history.size() - 1);

        ShippingResponse response = new ShippingResponse();
        response.setCarrier(sweetResponse.getCompany().getName());
        response.setTrackingNo(sweetResponse.getInvoiceNo());
        response.setTrackingHistory(history);
        response.setLastEvent(lastEvent);
        return response;
    }

    private ShippingStatus mapLevelToStatus(Integer level) {
        if (level == null) return ShippingStatus.UNKNOWN;
        switch (level) {
            case 1:
            case 2:
                return ShippingStatus.READY;
            case 3:
            case 4:
                return ShippingStatus.SHIPPING;
            case 5:
            case 6:
                return ShippingStatus.DONE;
            default:
                return ShippingStatus.UNKNOWN;
        }
    }

    public static List<String> getSupportedCarriers() {
        return new java.util.ArrayList<>(CARRIER_CODE_MAP.keySet());
    }
}
