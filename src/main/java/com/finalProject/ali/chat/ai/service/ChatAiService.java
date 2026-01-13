package com.finalProject.ali.chat.ai.service;

import com.finalProject.ali.chat.ai.dto.AiDraftRequestDTO;
import com.finalProject.ali.chat.ai.dto.AiSuggestionsResponseDTO;

public interface ChatAiService {

    AiSuggestionsResponseDTO getSuggestions(Long roomId, String myUserId, Long productId);

    String generateDraft(Long roomId, String myUserId, AiDraftRequestDTO req);

    Long resolveProductId(Long roomId, Long requestedProductId);
}
