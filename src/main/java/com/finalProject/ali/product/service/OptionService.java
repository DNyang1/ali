package com.finalProject.ali.product.service;

import com.finalProject.ali.product.dto.OptionDTO;
import com.finalProject.ali.product.dto.StockOptionDTO;

import java.util.List;
import java.util.Map;

public interface OptionService {

    List<OptionDTO> getOptions(Long productId);

    Map<String, List<OptionDTO>> getGroupOptions(Long productId);

    Map<String, List<StockOptionDTO>> getStockOptions(Long productId);

}
