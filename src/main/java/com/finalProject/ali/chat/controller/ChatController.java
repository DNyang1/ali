package com.finalProject.ali.chat.controller;

import com.finalProject.ali.chat.dto.ChatDTO;
import com.finalProject.ali.chat.dto.ChatSendDTO;
import com.finalProject.ali.chat.dto.UnreadTotalDTO;
import com.finalProject.ali.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

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
        String senderName = chatService.getUserName(req.getSenderId()); // users에서 조회
        payload.setSenderName(senderName);

        messagingTemplate.convertAndSend("/topic/rooms/" + req.getRoomId(), payload);

        String sender = normalizeUserId(req.getSenderId());
        List<String> members = chatService.getMemberIds(req.getRoomId());

        if (members != null) {
            for (String m : members) {
                String member = normalizeUserId(m);

                // 보낸 사람은 제외(본인 unread 안 늘어남)
                if (member != null && member.equals(sender)) continue;

                long total = chatService.getUnreadTotal(member);
                messagingTemplate.convertAndSend(
                        "/topic/users/" + member + "/unread-total",
                        new UnreadTotalDTO(total)
                );
            }
        }
    }

    private String normalizeUserId(String userId) {
        if (userId == null) return null;
        return userId.startsWith("s_") ? userId.substring(2) : userId;
    }

}
