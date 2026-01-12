package com.finalProject.ali.chat.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiDraftResponseDTO {
    private String draft; // 입력창에 그대로 넣을 완성 문장
}
