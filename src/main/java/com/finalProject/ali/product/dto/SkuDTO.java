package com.finalProject.ali.product.dto;

import com.finalProject.ali.product.dto.SkuPriceDTO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SkuDTO {
    //공통
    private String skuId;
    private Long productId;
    private Long stockQuantity;
    //태민
    private LocalDate createdAt;
    private String status;
    private String displayStatus;
    private String optionSummary;
    private BigDecimal basePrice;
    private boolean editable;
    private Long moq;

    // 현성
    private List<String> optionValueIds = new ArrayList<>();
    private List<SkuPriceDTO> priceRules = new ArrayList<>();
}
