package com.finalProject.ali.chat.controller;

import com.finalProject.ali.chat.dto.ChatDTO;
import com.finalProject.ali.chat.dto.RoomListDTO;
import com.finalProject.ali.chat.service.ChatService;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/api")
public class ChatRestController {

    private final ChatService chatService;

    // 내 방 리스트
    @GetMapping("/rooms")
    public List<RoomListDTO> myRooms(HttpSession session) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return chatService.getMyRooms(loginUser.getUserId());
    }

    // 특정 방 메시지 조회
    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatDTO> messages(@PathVariable Long roomId, HttpSession session) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String userId = loginUser.getUserId();
        if (!chatService.isMember(roomId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return chatService.getChatsForRoomWithReadStatus(roomId, userId);
    }

    // 읽음 처리
    @PostMapping("/rooms/{roomId}/read")
    public void markAsRead(@PathVariable Long roomId, HttpSession session) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String userId = loginUser.getUserId();
        if (!chatService.isMember(roomId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        chatService.markAsRead(roomId, userId);
    }
}
