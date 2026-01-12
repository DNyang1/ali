package com.finalProject.ali.product.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class OpenAiClient {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * GPT Chat Completion 호출
     * @param prompt GPT에게 전달할 프롬프트
     * @return GPT 응답 텍스트 (content)
     */
    public String chat(String prompt) {

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "user",
                    "content", prompt
            ));

            requestBody.put("messages", messages);

            requestBody.put("temperature", 0.3);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions",
                    request,
                    Map.class
            );

            // 4️⃣ 응답 파싱
            if (response.getBody() == null) {
                return "";
            }

            List<?> choices = (List<?>) response.getBody().get("choices");
            if (choices == null || choices.isEmpty()) {
                return "";
            }

            Map<?, ?> choice = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) choice.get("message");

            if (message == null || message.get("content") == null) {
                return "";
            }

            return message.get("content").toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
