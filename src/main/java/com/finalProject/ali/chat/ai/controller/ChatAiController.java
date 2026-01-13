package com.finalProject.ali.chat.ai.controller;

import com.finalProject.ali.chat.ai.dto.AiDraftRequestDTO;
import com.finalProject.ali.chat.ai.dto.AiDraftResponseDTO;
import com.finalProject.ali.chat.ai.dto.AiSuggestionsResponseDTO;
import com.finalProject.ali.chat.ai.service.ChatAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/api/rooms/{roomId}/ai")
public class ChatAiController {

    private final ChatAiService chatAiService;

    @GetMapping("/suggestions")
    public ResponseEntity<AiSuggestionsResponseDTO> suggestions(
            @PathVariable Long roomId,
            @RequestParam(required = false) Long productId,
            Authentication auth
    ) {
        String myUserId = (auth != null ? auth.getName() : null);
        return ResponseEntity.ok(chatAiService.getSuggestions(roomId, myUserId, productId));
    }

    @PostMapping("/draft")
    public ResponseEntity<AiDraftResponseDTO> draft(
            @PathVariable Long roomId,
            @RequestBody AiDraftRequestDTO req,
            Authentication auth
    ) {
        String myUserId = (auth != null ? auth.getName() : null);
        String draft = chatAiService.generateDraft(roomId, myUserId, req);
        return ResponseEntity.ok(new AiDraftResponseDTO(draft));
    }
}
