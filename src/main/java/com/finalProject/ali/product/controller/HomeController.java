package com.finalProject.ali.product.controller;

import com.finalProject.ali.product.category.service.CategoryService;
import com.finalProject.ali.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

        model.addAttribute(
                "recommendByCategory",
                productService.getRecommendedProducts(4)
        );

        return "index/index";
    }
}
