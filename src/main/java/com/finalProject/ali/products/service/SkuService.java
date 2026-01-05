package com.finalProject.ali.products.service;

import com.finalProject.ali.products.dto.SkusDTO;

import java.util.List;

public interface SkuService {

    List<SkusDTO> getSkuWithPrices(Long productId);
}
