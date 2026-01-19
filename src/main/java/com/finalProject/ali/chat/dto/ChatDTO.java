package com.finalProject.ali.chat.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ChatDTO {
    private Long chatId;
    private Long roomId;
    private String senderId;
    private String message;
    private LocalDateTime chatAt;
    private Boolean readByOpponent; // 내가 보낸 메시지를 상대가 읽었는지
    private Long productId;
    private String senderName;
    private String senderProfileImg;
}
