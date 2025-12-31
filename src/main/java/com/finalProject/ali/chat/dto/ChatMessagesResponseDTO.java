package com.finalProject.ali.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatMessagesResponseDTO {
    private List<ChatDTO> messages;
    private Long opponentLastReadChatId; // 상대가 이 방에서 마지막으로 읽은 chat_id
}
