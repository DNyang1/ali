package com.finalProject.ali.products.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SkusDTO {
    private String skuId;
    private Long productId;
    private int stockQuantity;

    private List<String> optionValueIds = new ArrayList<>();

    private List<SkusPriceDTO> priceRules = new ArrayList<>();
}
