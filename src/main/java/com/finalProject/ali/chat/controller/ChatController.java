package com.finalProject.ali.chat.controller;

import com.finalProject.ali.chat.dto.ChatDTO;
import com.finalProject.ali.chat.dto.ChatSendDTO;
import com.finalProject.ali.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // 보내는 쪽: /app/chat.send
    // 받는 쪽(구독): /topic/rooms/{roomId}

    @MessageMapping("/chat.send")
    public void send(ChatSendDTO req) {
        log.info("WS recv => {}", req);

        if (req.getRoomId() == null) return;
        if (req.getSenderId() == null || req.getSenderId().isBlank()) return;
        if ((req.getMessage() == null || req.getMessage().isBlank()) && req.getProductId() == null) return;

        // 방 멤버 검증
        if (!chatService.isMember(req.getRoomId(), req.getSenderId())) return;

        Long chatId = chatService.saveChat(req.getRoomId(), req.getSenderId(), req.getMessage(), req.getProductId());
        log.info("DB saved chatId={}", chatId);

        ChatDTO payload = new ChatDTO();
        payload.setChatId(chatId);
        payload.setRoomId(req.getRoomId());
        payload.setSenderId(req.getSenderId());
        payload.setMessage(req.getMessage());
        payload.setProductId(req.getProductId());
        payload.setChatAt(LocalDateTime.now());

        messagingTemplate.convertAndSend("/topic/rooms/" + req.getRoomId(), payload);
//        log.info("WS sent => /topic/rooms/{}", req.getRoomId());
        log.info("WS recv productId => {}", req.getProductId());

    }

}
