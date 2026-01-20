package com.finalProject.ali.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventDTO {

    private Long eventId;
    private String eventType;
    private String targetType;
    private String targetId;
    private String discountType;
    private Long discountValue;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
