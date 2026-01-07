package com.finalProject.ali.inquiry.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InquiryCreateRequest {
    private Long productId;
    private Long quantity; // null 허용
    private String content;
}

