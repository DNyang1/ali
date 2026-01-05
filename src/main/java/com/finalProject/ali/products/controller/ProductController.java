package com.finalProject.ali.products.controller;

import com.finalProject.ali.products.dto.*;
import com.finalProject.ali.products.service.OptionService;
import com.finalProject.ali.products.service.ProductService;
import com.finalProject.ali.products.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final OptionService optionService;
    private final SkuService skuService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public String productsRoot() {
        return "redirect:/products/list";
    }

    @GetMapping("/list")
    public String productsList(
            @RequestParam(required = false) Boolean custom,
            @RequestParam(required = false) String category,
            Model model) {

        List<ProductsDTO> products;

        if (category != null && custom != null) {
            products = productService.getProductsByCategoryAndCustom(category, custom);

        } else if (category != null) {
            products = productService.getProductsByRootCategory(category);

        } else if (custom != null) {
            products = productService.getProductsByCustom(custom);

        } else {
            products = productService.productList();
        }

        List<CategoryDTO> categories = productService.getRootCategories();


        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("custom", custom);
        model.addAttribute("category", category);

        return "products/products_list";
    }



    @GetMapping("/{productId}")
    public String productsDetail(@PathVariable Long productId, Model model) throws Exception {

        ProductsDTO product = productService.productDetail(productId);

        Map<String, List<OptionValueDTO>> options =
                optionService.getGroupOptions(productId);

        List<SkusDTO> skus =
                skuService.getSkuWithPrices(productId);

        List<SkusPriceDTO> defaultPriceRules =
                (!skus.isEmpty() && skus.get(0).getPriceRules() != null)
                        ? skus.get(0).getPriceRules()
                        : List.of();

        model.addAttribute("product", product);
        model.addAttribute("options", options);
        model.addAttribute(
                "requiredOptionCount",
                options == null ? 0 : options.size()
        );

        model.addAttribute(
                "skusJson",
                objectMapper.writeValueAsString(skus)
        );

        model.addAttribute(
                "defaultPriceRulesJson",
                objectMapper.writeValueAsString(defaultPriceRules)
        );

        return "products/products_detail";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam String keyword,
            Model model
    ) {
        List<ProductsDTO> products = productService.searchProducts(keyword);

        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);

        return "products/products_search";
    }

}
