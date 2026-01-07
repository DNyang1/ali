package com.finalProject.ali.product.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SkuForm {
    private Long stock;
    private Long basePrice;
    private Long moq =1L;
    private List<Range> ranges;
    private Map<String, String> selected;

}
