package com.finalProject.ali.mypage.supplier.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SkuPriceRequestDTO {

    private Integer basePrice;
    private List<RangeDTO> ranges;
    private Long moq;
    @Getter @Setter
    public static class RangeDTO {
        private Integer minQty;
        private Integer maxQty;
        private Integer price;
    }
}