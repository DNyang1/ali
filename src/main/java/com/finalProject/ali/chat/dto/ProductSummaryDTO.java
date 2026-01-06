package com.finalProject.ali.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProductSummaryDTO {
    private Long productId;
    private String productName;
    private String thumbnailUrl; // 지금은 null 가능
}

