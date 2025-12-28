package com.finalProject.ali.chat.controller;

import com.finalProject.ali.chat.dto.ChatDTO;
import com.finalProject.ali.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/api")
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatDTO> messages(@PathVariable Long roomId) {
        return chatService.getChatsByRoomId(roomId);
    }
}
