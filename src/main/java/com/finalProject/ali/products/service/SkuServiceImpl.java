package com.finalProject.ali.products.service;

import com.finalProject.ali.products.dao.SkusDAO;
import com.finalProject.ali.products.dao.SkusPriceDAO;
import com.finalProject.ali.products.dto.SkuRowDTO;
import com.finalProject.ali.products.dto.SkusDTO;
import com.finalProject.ali.products.dto.SkusPriceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkuServiceImpl implements SkuService{

    private final SkusDAO skusDAO;
    private final SkusPriceDAO skuPriceDAO;

    @Override
    public List<SkusDTO> getSkuWithPrices(Long productId) {

        // 1) SKU + 옵션값 조합 (row 단위)
        List<SkuRowDTO> rows = skusDAO.findSkuRowsByProductId(productId);

        // SKU가 없을 수도 있음
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        // 2) SKU 그룹핑
        Map<String, SkusDTO> map = new LinkedHashMap<>();

        for (SkuRowDTO r : rows) {

            SkusDTO sku = map.computeIfAbsent(r.getSkuId(), k -> {
                SkusDTO s = new SkusDTO();
                s.setSkuId(r.getSkuId());
                s.setProductId(r.getProductId());

                // ✅ Integer → int 안전 처리
                s.setStockQuantity(
                        r.getStockQuantity() != null ? r.getStockQuantity() : 0
                );

                return s;
            });

            // 옵션값 연결
            if (r.getOptionValueId() != null) {
                sku.getOptionValueIds().add(r.getOptionValueId());
            }
        }

        // 2) SKU 그룹핑 결과
        List<SkusDTO> skus = new ArrayList<>(map.values());

        // 3) 가격 규칙 한 번에 가져와서 skuId로 묶기
        List<SkusPriceDTO> priceRows =
                skuPriceDAO.findPriceRulesByProductId(productId);

        List<SkusPriceDTO> safePriceRows =
                (priceRows == null) ? Collections.emptyList() : priceRows;

        Map<String, List<SkusPriceDTO>> priceMap =
                safePriceRows.stream()
                        .collect(Collectors.groupingBy(
                                SkusPriceDTO::getSkuId,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));


        // 4) SKU에 가격 규칙 주입
        for (SkusDTO sku : skus) {
            List<SkusPriceDTO> rules =
                    priceMap.getOrDefault(sku.getSkuId(), Collections.emptyList());
            sku.setPriceRules(rules);
        }

        return skus;
    }
}
