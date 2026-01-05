package com.finalProject.ali.mypage.supplier.product.dto;

import lombok.Data;

import java.util.List;
@Data
public class SkuForm {
    private Long stock;
    private Long basePrice;
    private Long moq =1L;
    private List<Range> ranges;
    private List<String> optionValueIds;
}
