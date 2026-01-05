package com.finalProject.ali.products.service;

import com.finalProject.ali.products.dao.OptionsDAO;
import com.finalProject.ali.products.dto.OptionValueDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OptionServiceImpl implements OptionService{

    private final OptionsDAO optionsDAO;

    @Override
    public List<OptionValueDTO> getOptions(Long productId) {
        return optionsDAO.optionByProduct(productId);
    }

    @Override
    public Map<String, List<OptionValueDTO>> getGroupOptions(Long productId) {

        List<OptionValueDTO> rows = optionsDAO.optionByProduct(productId);

        System.out.println("==== OPTION ROWS ====");
        rows.forEach(System.out::println);

        if (rows.isEmpty()) {
            return Map.of();
        }

        return rows.stream()
                .collect(Collectors.groupingBy(
                        OptionValueDTO::getOptionName,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

}
