package com.finalProject.ali.chat.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AiSuggestionsResponseDTO {
    private List<SuggestionItem> suggestions;

    @Getter
    @AllArgsConstructor
    public static class SuggestionItem {
        private String key;     // e.g. "MOQ", "PRICE"
        private String text;    // 버튼에 보일 짧은 문장(1줄)
    }
}
