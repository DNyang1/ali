package com.finalProject.ali.chat.controller;

import com.finalProject.ali.chat.dto.ChatDTO;
import com.finalProject.ali.chat.dto.RoomListDTO;
import com.finalProject.ali.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatPageController {

    private final ChatService chatService;

    @GetMapping("/chat/messages")
    public String messages(@RequestParam(required = false) Long roomId,
                           @RequestParam(defaultValue = "testUser") String userId, // TODO: principal로 교체
                           Model model) {

        model.addAttribute("currentUserId", userId);

        List<RoomListDTO> rooms = chatService.getMyRooms(userId);
        model.addAttribute("rooms", rooms);

        Long currentRoomId = roomId;
        if (currentRoomId == null && !rooms.isEmpty()) {
            currentRoomId = rooms.get(0).getRoomId();
        }
        model.addAttribute("currentRoomId", currentRoomId);

        List<ChatDTO> messages = Collections.emptyList();
        if (currentRoomId != null) {
            messages = chatService.getChatsByRoomId(currentRoomId);
        }
        model.addAttribute("messages", messages);

        if (currentRoomId != null) {
            model.addAttribute("currentRoomTitle", "Room #" + currentRoomId);
            model.addAttribute("currentRoomSub", "");
        } else {
            model.addAttribute("currentRoomTitle", "채팅방이 없습니다.");
            model.addAttribute("currentRoomSub", "");
        }

        return "chat/messages";
    }
}
