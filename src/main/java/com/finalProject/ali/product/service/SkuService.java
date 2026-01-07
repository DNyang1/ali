package com.finalProject.ali.product.service;

import com.finalProject.ali.product.dto.SkuDTO;

import java.util.List;

public interface SkuService {

    List<SkuDTO> getSkuWithPrices(Long productId);

    SkuDTO findSkuByOptionValues(List<String> optionValueIds);
}
