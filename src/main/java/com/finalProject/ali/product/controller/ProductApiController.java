package com.finalProject.ali.product.controller;

import com.finalProject.ali.product.dto.*;
import com.finalProject.ali.product.service.ProductService;
import com.finalProject.ali.product.service.SkuPriceService;
import com.finalProject.ali.product.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/products")
public class ProductApiController {

    private final ProductService productService;
    private final SkuService skuService;
    private final SkuPriceService skuPriceService;

    @GetMapping("/preview")
    public List<ProductDTO> previewByCategory(
            @RequestParam String categoryId
    ) {
        return productService.getProductsByCategory(categoryId);
    }

    @PostMapping("/sku/match")
    public ResponseEntity<SkuDTO> matchSku(
            @RequestBody SkuMatchRequestDTO dto
    ) {
        SkuDTO sku = skuService.findSkuByOptionValues(dto.getOptionValueIds());

        if (sku == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(sku);
    }

    @PostMapping("/sku/price")
    public ResponseEntity<SkuPriceDTO> calcPrice(
            @RequestBody SkuPriceCalcRequestDTO dto
    ) {
        return ResponseEntity.ok(
                skuPriceService.calculatePrice(dto.getSkuId(), dto.getQty())
        );
    }

}
