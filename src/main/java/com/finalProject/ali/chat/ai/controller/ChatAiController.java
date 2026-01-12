package com.finalProject.ali.chat.ai.controller;

import com.finalProject.ali.chat.ai.dto.AiDraftRequestDTO;
import com.finalProject.ali.chat.ai.dto.AiDraftResponseDTO;
import com.finalProject.ali.chat.ai.service.ChatAiService;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/api/rooms/{roomId}/ai")
public class ChatAiController {

    private final ChatAiService chatAiService;

    @PostMapping("/draft")
    public AiDraftResponseDTO draft(
            @PathVariable Long roomId,
            @RequestBody AiDraftRequestDTO req,
            HttpSession session
    ) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        try {
            String draft = chatAiService.makeDraft(roomId, loginUser.getUserId(), req);
            return new AiDraftResponseDTO(draft);
        } catch (IllegalArgumentException e) {
            if ("FORBIDDEN".equals(e.getMessage())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/suggestions")
    public Map<String, Object> suggestions(
            @PathVariable Long roomId,
            @RequestParam(required = false) Long productId,
            HttpSession session
    ) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        // room 멤버 체크(너 프로젝트 방식에 맞게)
        // if (!chatService.isMember(roomId, loginUser.getUserId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        var suggestions = java.util.List.of(
                java.util.Map.of("key", "MOQ", "text", "최소 주문 수량(MOQ)이 어떻게 되나요?"),
                java.util.Map.of("key", "PRICE", "text", "수량별 단가(가격표) 받을 수 있을까요?"),
                java.util.Map.of("key", "LEAD_TIME", "text", "납기(리드타임)는 어느 정도 걸리나요?"),
                java.util.Map.of("key", "SAMPLE", "text", "샘플 제공이 가능한가요?")
        );

        return java.util.Map.of("suggestions", suggestions);
    }

}
