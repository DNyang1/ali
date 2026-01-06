package com.finalProject.ali.product.controller;

import com.finalProject.ali.product.dto.ProductDTO;
import com.finalProject.ali.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/products")
public class ProductApiController {

    private final ProductService productService;

    @GetMapping("/preview")
    public List<ProductDTO> previewByCategory(
            @RequestParam String categoryId
    ) {
        return productService.getProductsByCategory(categoryId);
    }

}
