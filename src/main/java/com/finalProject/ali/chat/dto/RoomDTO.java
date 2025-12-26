package com.finalProject.ali.chat.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class RoomDTO {
    private Long roomId;
    private LocalDateTime createdAt;
}
