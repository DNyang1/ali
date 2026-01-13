package com.finalProject.ali.chat.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class ChatOpenAiClient {

    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model:gpt-5-mini}")
    private String model;

    public ChatOpenAiClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://api.openai.com/v1")
                .build();
    }

    public String generateText(String instructions, String userText) {

        // Responses API: instructions + input(문자열) 형태로 호출 가능
        Map<String, Object> body = Map.of(
                "model", model,
                "instructions", instructions,
                "input", userText,
                "max_output_tokens", 200
        );

        try {
            Map<?, ?> res = webClient.post()
                    .uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> h.setBearerAuth(apiKey))
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return extractOutputText(res);

        } catch (Exception e) {
            // ✅ 여기서 예외를 밖으로 던지면 추천버튼이 사라질 확률이 높음
            return "";
        }
    }

    /**
     * OpenAI Responses API 응답(Map)에서 사람이 읽을 "최종 텍스트"만 뽑아내는 함수.
     * 보통 res["output"] 배열 안에 text content가 들어있음.
     */
    @SuppressWarnings("unchecked")
    private String extractOutputText(Map<?, ?> res) {
        if (res == null) return "";

        Object outputObj = res.get("output");
        if (!(outputObj instanceof List<?> output)) return "";

        StringBuilder sb = new StringBuilder();

        for (Object item : output) {
            if (!(item instanceof Map<?, ?> om)) continue;

            Object contentObj = om.get("content");
            if (!(contentObj instanceof List<?> contentList)) continue;

            for (Object c : contentList) {
                if (!(c instanceof Map<?, ?> cm)) continue;

                // 보통 type:"output_text" / text:"..."
                Object type = cm.get("type");
                if (type != null && "output_text".equals(type.toString())) {
                    Object text = cm.get("text");
                    if (text != null) sb.append(text.toString());
                }
            }
        }

        String out = sb.toString().trim();
        return (StringUtils.hasText(out) ? out : "");
    }
}
