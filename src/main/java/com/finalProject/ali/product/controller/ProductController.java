package com.finalProject.ali.product.controller;

import com.finalProject.ali.product.dto.*;
import com.finalProject.ali.product.service.OptionService;
import com.finalProject.ali.product.service.ProductService;
import com.finalProject.ali.product.service.SkuPriceService;
import com.finalProject.ali.product.service.SkuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final OptionService optionService;
    private final SkuService skuService;
    private final SkuPriceService skuPriceService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public String productsRoot() {
        return "redirect:/products/list";
    }

    @GetMapping("/list")
    public String productsList(
            @RequestParam(required = false) Boolean custom,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean discount,
            Model model) {

        List<ProductDTO> products;
        String pageTitle = "전체 상품";

        if (Boolean.TRUE.equals(discount) && category != null) {
            products = productService.getDiscountProductsByCategory(category);
            pageTitle = "할인 상품";

        } else if (Boolean.TRUE.equals(discount)) {
            products = productService.getDiscountProducts();
            pageTitle = "할인 상품";

        } else if (custom != null && category != null) {
            products = productService.getProductsByCategoryAndCustom(category, custom);
            pageTitle = (custom ? "커스텀 " : "Ali ") + "카테고리 상품";

        } else if (category != null) {
            products = productService.getProductsByRootCategory(category);
            pageTitle = "카테고리 상품";

        } else if (Boolean.TRUE.equals(custom)) {
            products = productService.getProductsByCustom(true);
            pageTitle = "커스텀 상품";

        } else if (Boolean.FALSE.equals(custom)) {
            products = productService.getProductsByCustom(false);
            pageTitle = "Ali 상품";

        } else {
            products = productService.productList();
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getRootCategories());
        model.addAttribute("custom", custom);
        model.addAttribute("category", category);
        model.addAttribute("discount", discount);
        model.addAttribute("pageTitle", pageTitle);

        return "products/products_list";
    }




    @GetMapping("/{productId:\\d+}")
    public String productsDetail(
            @PathVariable Long productId,
            Model model
    ) throws Exception {

        ProductDTO product =
                productService.productDetail(productId);

        log.info(
                "[EVENT CHECK] productId={}, discountType={}, discountValue={}, endAt={}",
                productId,
                product.getDiscountType(),
                product.getDiscountValue(),
                product.getEventEndAt()
        );


        Map<String, List<StockOptionDTO>> options =
                optionService.getStockOptions(productId);

        List<SkuDTO> skus =
                skuService.getProductDetailSkus(productId);

        List<SkuPriceDTO> defaultPriceRules =
                skuPriceService.getPriceRulesByProductId(productId);

        model.addAttribute("product", product);
        model.addAttribute("options", options);
        model.addAttribute("requiredOptionCount", options.size());
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

        List<ProductDTO> products =
                productService.searchProducts(keyword);

        ObjectMapper objectMapper = new ObjectMapper();
        String productsJson = objectMapper.writeValueAsString(products);

        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("productsJson", productsJson);

        return "products/products_search";
    }

}
