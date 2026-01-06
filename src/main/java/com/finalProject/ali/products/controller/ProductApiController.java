package com.finalProject.ali.products.controller;

import com.finalProject.ali.products.dto.*;
import com.finalProject.ali.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/products")
public class ProductApiController {

    private final ProductService productService;

    @GetMapping("/preview")
    public List<ProductsDTO> previewByCategory(
            @RequestParam String categoryId
    ) {
        return productService.getProductsByCategory(categoryId);
    }

}
