package com.finalProject.ali.chat.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatOpenAiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model:gpt-5-mini}")
    private String model;

    public String generateDraft(String instructions, String userText) {
        Map<String, Object> body = Map.of(
                "model", model,
                "instructions", instructions,
                "input", userText
        );

        Map<?, ?> res = webClientBuilder.build()
                .post()
                .uri("https://api.openai.com/v1/responses")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                // ✅ 4xx/5xx면 OpenAI가 준 에러 본문을 그대로 예외로 던져서 원인 확인
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .map(msg -> new RuntimeException("OpenAI error: " + msg)))
                .bodyToMono(Map.class)
                .block();

        return extractOutputText(res);
    }

    @SuppressWarnings("unchecked")
    private String extractOutputText(Map<?, ?> res) {
        if (res == null) return null;

        // 가끔 output_text가 바로 들어오는 케이스가 있어서 우선 사용
        Object ot = res.get("output_text");
        if (ot instanceof String s && !s.isBlank()) return s;

        Object outputObj = res.get("output");
        if (!(outputObj instanceof List<?> output)) return null;

        StringBuilder sb = new StringBuilder();

        for (Object itemObj : output) {
            if (!(itemObj instanceof Map<?, ?> item)) continue;

            // message 타입만
            Object type = item.get("type");
            if (!"message".equals(type)) continue;

            Object contentObj = item.get("content");
            if (!(contentObj instanceof List<?> content)) continue;

            for (Object cObj : content) {
                if (!(cObj instanceof Map<?, ?> c)) continue;

                if (!"output_text".equals(c.get("type"))) continue;

                Object text = c.get("text");
                if (text instanceof String t && !t.isBlank()) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(t);
                }
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }
}
