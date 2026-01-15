package com.finalProject.ali.chat.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
public class RoomListDTO {
    private Long roomId;
    private String lastMessage;
    private String lastSenderId;
    private LocalDateTime lastChatAt;
    private Long unreadCount;
    private String opponentId;
    private String opponentName;
    private String opponentProfileImg;
}

