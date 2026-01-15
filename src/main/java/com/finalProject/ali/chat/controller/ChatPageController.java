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

    @GetMapping("/chat/messages-user")
    public String messagesUser(@RequestParam(required = false) Long roomId,
                               HttpSession session, Model model) {
        model.addAttribute("activeMenu", "chat");
        model.addAttribute("pageTitle", "채팅");
        return renderMessagesPage(roomId, session, model, "chat/messages-user");

    }

    @GetMapping("/chat/messages-supplier")
    public String messagesSupplier(@RequestParam(required = false) Long roomId,
                                   HttpSession session, Model model) {
        model.addAttribute("activeMenu", "chat");
        model.addAttribute("pageTitle", "채팅");
        return renderMessagesPage(roomId, session, model, "chat/messages-supplier");
    }

    private String renderMessagesPage(Long roomId, HttpSession session, Model model, String viewName) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/user/login";
        }

        String userId = loginUser.getUserId();
        model.addAttribute("currentUserId", userId);

        List<RoomListDTO> rooms = chatService.getMyRooms(userId);

        Long currentRoomId = validateRoom(roomId, rooms);

        if (currentRoomId != null) {
            chatService.markAsRead(currentRoomId, userId);
            rooms = chatService.getMyRooms(userId);
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("currentRoomId", currentRoomId);

        List<ChatDTO> messages = Collections.emptyList();
        if (currentRoomId != null) {
            messages = chatService.getChatsByRoomId(currentRoomId);
            chatService.markAsRead(currentRoomId, userId);
        }
        model.addAttribute("messages", messages);

        model.addAttribute("currentRoomTitle",
                currentRoomId != null ? "Room #" + currentRoomId : "채팅방이 없습니다.");
        model.addAttribute("currentRoomSub", "");

        return viewName;
    }

    private Long validateRoom(Long roomId, List<RoomListDTO> rooms) {
        if (roomId == null) return null;
        for (RoomListDTO r : rooms) {
            if (r.getRoomId().equals(roomId)) return roomId;
        }
        return null;
    }
}
