package com.finalProject.ali.chat.controller;

import com.finalProject.ali.chat.dto.ChatDTO;
import com.finalProject.ali.chat.dto.RoomListDTO;
import com.finalProject.ali.chat.service.ChatService;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
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
    public String messages(@RequestParam(required = false) Long roomId, HttpSession session, Model model) {
        //  로그인 유저 (세션에서 UserDTO로 꺼내기)
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/user/login";
        }

        String userId = loginUser.getUserId();
        model.addAttribute("currentUserId", userId);

        // 1) 방 목록 먼저 조회 (검증용)
        List<RoomListDTO> rooms = chatService.getMyRooms(userId);

        // 2) 내 방인지 검증
        Long currentRoomId = getALong(roomId, rooms);

        // 3) 읽음 처리
        if (currentRoomId != null) {
            chatService.markAsRead(currentRoomId, userId);

            // 4) 읽음 반영된 unreadCount 다시 조회
            rooms = chatService.getMyRooms(userId);
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("currentRoomId", currentRoomId);


        // 메시지 조회
        List<ChatDTO> messages = Collections.emptyList();
        if (currentRoomId != null) {
            messages = chatService.getChatsByRoomId(currentRoomId);
            chatService.markAsRead(currentRoomId, userId);
        }
        model.addAttribute("messages", messages);

        model.addAttribute("currentRoomTitle",
                currentRoomId != null ? "Room #" + currentRoomId : "채팅방이 없습니다.");
        model.addAttribute("currentRoomSub", "");

        return "chat/messages";
    }

    private static @Nullable Long getALong(Long roomId, List<RoomListDTO> rooms) {
        if (roomId == null) return null;

        for (RoomListDTO r : rooms) {
            if (r.getRoomId().equals(roomId)) {
                return roomId; // 내 방이면 OK
            }
        }
        return null; // 내 방 아니면 null
    }

}
