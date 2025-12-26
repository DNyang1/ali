package com.finalProject.ali.chat.dto;

import lombok.Data;

@Data
public class ChatSendDTO {
    private Long roomId;
    private String message;
    private String senderId;  // 임시
}
