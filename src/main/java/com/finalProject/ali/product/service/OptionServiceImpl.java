package com.finalProject.ali.product.service;

import com.finalProject.ali.product.dao.OptionDAO;
import com.finalProject.ali.product.dto.OptionDTO;
import com.finalProject.ali.product.service.OptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OptionServiceImpl implements OptionService {

    private final OptionDAO optionDAO;

    @Override
    public List<OptionDTO> getOptions(Long productId) {
        return optionDAO.optionByProduct(productId);
    }

    @Override
    public Map<String, List<OptionDTO>> getGroupOptions(Long productId) {

        List<OptionDTO> rows = optionDAO.optionByProduct(productId);

        System.out.println("==== OPTION ROWS ====");
        rows.forEach(System.out::println);

        if (rows.isEmpty()) {
            return Map.of();
        }

        return rows.stream()
                .collect(Collectors.groupingBy(
                        OptionDTO::getOptionName,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

}
