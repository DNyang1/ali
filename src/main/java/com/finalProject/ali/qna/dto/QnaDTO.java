package com.finalProject.ali.qna.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QnaDTO {
    private Long qnaId;
    private String title;
    private String content;
    private String writerId;
    private String answer;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime answeredAt;
}