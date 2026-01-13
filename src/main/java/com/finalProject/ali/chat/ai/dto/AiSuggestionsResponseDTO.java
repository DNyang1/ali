package com.finalProject.ali.chat.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AiSuggestionsResponseDTO {
    private List<AiSuggestionDTO> suggestions;
}
