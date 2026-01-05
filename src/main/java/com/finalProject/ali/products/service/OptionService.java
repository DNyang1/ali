package com.finalProject.ali.products.service;

import com.finalProject.ali.products.dto.OptionValueDTO;

import java.util.List;
import java.util.Map;

public interface OptionService {

    List<OptionValueDTO> getOptions(Long productId);

    Map<String, List<OptionValueDTO>> getGroupOptions(Long productId);
}
