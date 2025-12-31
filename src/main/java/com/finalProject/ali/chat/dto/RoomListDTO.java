package com.finalProject.ali.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoomListDTO {
    private Long roomId;
    private String lastMessage;
    private String lastSenderId;
    private LocalDateTime lastChatAt;
    private Long unreadCount;
}

