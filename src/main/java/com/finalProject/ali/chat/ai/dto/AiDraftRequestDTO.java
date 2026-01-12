package com.finalProject.ali.chat.ai.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AiDraftRequestDTO {
    private Long productId;    // optional
    private String purpose;    // "MOQ" | "PRICE" | "LEADTIME" | "SAMPLE" | "SHIPPING" | "PAYMENT" | "QUOTE"
}
