package com.finalProject.ali.products.controller;

import com.finalProject.ali.products.dto.ProductsDTO;
import com.finalProject.ali.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    public String productsList(Model model) {

        List<ProductsDTO> products = productService.productList();
        model.addAttribute("products", products);

        return "products/products_list";
    }
}
