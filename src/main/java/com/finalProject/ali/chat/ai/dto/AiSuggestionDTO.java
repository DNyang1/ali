// com.finalProject.ali.chat.ai.dto.ChatAiSuggestion
package com.finalProject.ali.chat.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiSuggestionDTO {
    private String key;   // "PRICE", "MOQ" 등 (purpose)
    private String text;  // 칩에 보여줄 짧은 문장
}
