package com.finalProject.ali.chat.ai.controller;

import com.finalProject.ali.chat.ai.service.ChatOpenAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AiTestController {

    private final ChatOpenAiClient chatOpenAiClient;

    @GetMapping("/ai/test")
    public String test() {
        String instructions = "너는 테스트용 AI다. 짧게 한 문장으로 대답해라.";
        return chatOpenAiClient.generateDraft(instructions, "안녕? 테스트 중이야.");
    }

}
