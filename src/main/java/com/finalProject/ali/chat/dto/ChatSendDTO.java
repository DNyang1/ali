package com.finalProject.ali.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChatSendDTO {
    private Long roomId;
    private String message;
    private String senderId;
    private Long productId;
}
