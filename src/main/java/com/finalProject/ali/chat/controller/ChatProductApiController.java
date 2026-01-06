package com.finalProject.ali.chat.controller;

import com.finalProject.ali.chat.dao.ChatProductSummaryDAO;
import com.finalProject.ali.chat.dto.ProductSummaryDTO;
import com.finalProject.ali.products.dto.ProductsDTO;
import com.finalProject.ali.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ChatProductApiController {

    private final ChatProductSummaryDAO chatProductSummaryDAO;

    @GetMapping("/{productId}/summary")
    public ProductSummaryDTO summary(@PathVariable Long productId) {
        ProductSummaryDTO dto = chatProductSummaryDAO.findProductSummary(productId);
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return dto;
    }
}
