package com.finalProject.ali.chat.dto;

import lombok.Data;

@Data
public class ReadEventDTO {
    private Long roomId;
    private String readerId;      // 읽은 사람
    private Long lastReadChatId;  // 여기까지 읽음
}
