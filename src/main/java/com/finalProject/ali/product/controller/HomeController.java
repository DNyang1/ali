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

        Map<String, List<ProductDTO>> raw =
                productService.getRecommendedProducts(4);

        Map<String, List<List<ProductDTO>>> recommendByCategory = new LinkedHashMap<>();

        for (Map.Entry<String, List<ProductDTO>> entry : raw.entrySet()) {
            List<ProductDTO> products = entry.getValue();

            List<List<ProductDTO>> chunks = new ArrayList<>();
            for (int i = 0; i < products.size(); i += 4) {
                chunks.add(
                        products.subList(i, Math.min(i + 4, products.size()))
                );
            }

            recommendByCategory.put(entry.getKey(), chunks);
        }

        model.addAttribute(
                "recommendByCategory",
                productService.getRecommendedProducts(4)
        );

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
