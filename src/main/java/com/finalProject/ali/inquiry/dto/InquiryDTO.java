package com.finalProject.ali.inquiry.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class InquiryDTO {
    private Long inquiryId;
    private String userId;
    private String supplierId;
    private Long productId;
    private Long quantity;
    private String content;
    private Long status;
    private LocalDateTime createdAt;
}

