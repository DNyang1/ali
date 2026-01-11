package com.finalProject.ali.product.controller;

import com.finalProject.ali.product.dto.ProductSearchSummaryDTO;
import com.finalProject.ali.product.service.AiSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiSearchController {

    private final AiSearchService aiSearchService;

    /**
     * 검색 결과 기반 AI Deep Search 요약 API
     */
    @PostMapping("/search-summary")
    public String searchSummary(
            @RequestParam String keyword,
            @RequestBody List<ProductSearchSummaryDTO> products
    ) {
        return aiSearchService.makeSearchSummaryText(keyword, products);
    }
}
