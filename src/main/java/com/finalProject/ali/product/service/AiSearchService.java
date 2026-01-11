package com.finalProject.ali.product.service;

import com.finalProject.ali.product.dto.AiSearchResultDTO;
import com.finalProject.ali.product.dto.ProductSearchSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiSearchService {

    private final OpenAiClient openAiClient;

    // JSON 파싱용
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 검색 결과 기반 AI Deep Search 요약 (사람용 문장 반환)
     */
    public String makeSearchSummaryText(
            String keyword,
            List<ProductSearchSummaryDTO> products
    ) {

        // 🔒 안전장치
        if (products == null || products.isEmpty()) {
            return null;
        }

        // 1️⃣ 카테고리 검색 여부 판별
        boolean isCategorySearch = isCategorySearch(products);

        // 2️⃣ GPT에 전달할 상품 요약 문자열 생성
        StringBuilder productText = new StringBuilder();
        for (ProductSearchSummaryDTO p : products) {
            productText.append("""
                - 상품명: %s
                  카테고리: %s
                  커스텀 가능: %s
                  가격대: %s
                """.formatted(
                    p.getName(),
                    p.getCategoryName(),
                    p.isCustom() ? "가능" : "불가",
                    p.getPriceRange()
            ));
        }

        // 3️⃣ 검색 타입 설명 (AI 힌트)
        String searchTypeHint = isCategorySearch
                ? "이번 검색은 상품명이 아닌 카테고리 기반 검색 결과입니다."
                : "이번 검색은 상품명 기반 검색 결과입니다.";

        // 4️⃣ JSON 응답 강제 프롬프트
        String prompt = """
        당신은 쇼핑 검색 AI입니다.

        사용자 검색어: "%s"

        %s

        검색된 상품 목록:
        %s

        반드시 아래 JSON 형식으로만 응답하세요.
        다른 문장, 설명, 마크다운은 절대 포함하지 마세요.

        {
          "intent": "검색 의도 요약",
          "strategy": "검색 전략 요약",
          "match": "상품과 키워드 일치 설명"
        }
        """.formatted(keyword, searchTypeHint, productText);

        try {
            // 5️⃣ GPT 호출
            String aiResponse = openAiClient.chat(prompt);

            // 6️⃣ JSON → DTO 파싱
            AiSearchResultDTO dto =
                    objectMapper.readValue(aiResponse, AiSearchResultDTO.class);

            // 7️⃣ UX용 문장 가공 (최종 반환)
            return """
            요구 사항 이해:
            - %s

            검색 전략:
            - %s

            상품 및 키워드 일치:
            - %s
            """.formatted(
                    dto.getIntent(),
                    dto.getStrategy(),
                    dto.getMatch()
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 카테고리 검색 여부 판단
     * - 모든 상품의 categoryName이 동일하면 카테고리 검색으로 간주
     */
    private boolean isCategorySearch(List<ProductSearchSummaryDTO> products) {

        if (products == null || products.isEmpty()) {
            return false;
        }

        Long firstCategoryId = products.get(0).getCategoryId();

        if (firstCategoryId == null) {
            return false;
        }

        return products.stream()
                .map(ProductSearchSummaryDTO::getCategoryId)
                .allMatch(id -> firstCategoryId.equals(id));
    }

}
