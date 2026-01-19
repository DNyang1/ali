package com.finalProject.ali.product.controller;

import com.finalProject.ali.product.category.service.CategoryService;
import com.finalProject.ali.product.dto.ProductDTO;
import com.finalProject.ali.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping("/")
    public String index(Model model){

        model.addAttribute(
                "categories",
                categoryService.getMainCategories()
        );

        Map<String, List<ProductDTO>> recommendByCategory = productService.getRecommendedProducts(4);
        model.addAttribute("recommendByCategory", recommendByCategory);

        model.addAttribute(
                "discountProductCount",
                productService.countActiveDiscountProducts()
        );

        model.addAttribute(
                "maxDiscountRate",
                productService.findMaxDiscountRate()
        );

        model.addAttribute("aliRandomProducts",
                productService.getRandomAliProducts());

        model.addAttribute("customRandomProducts",
                productService.getRandomCustomProducts());

        return "index/index";
    }
}
