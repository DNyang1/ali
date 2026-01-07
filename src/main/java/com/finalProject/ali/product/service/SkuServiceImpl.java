package com.finalProject.ali.product.service;

import com.finalProject.ali.product.dao.SkuDAO;
import com.finalProject.ali.product.dao.SkuPriceDAO;
import com.finalProject.ali.product.dto.SkuDTO;
import com.finalProject.ali.product.dto.SkuPriceDTO;
import com.finalProject.ali.product.dto.SkuRowDTO;
import com.finalProject.ali.product.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkuServiceImpl implements SkuService {

    private final SkuDAO skuDAO;
    private final SkuPriceDAO skuPriceDAO;

    @Override
    public List<SkuDTO> getSkuWithPrices(Long productId) {

        // 1) SKU + 옵션값 조합 (row 단위)
        List<SkuRowDTO> rows = skuDAO.findSkuRowsByProductId(productId);

        // SKU가 없을 수도 있음
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        // 2) SKU 그룹핑
        Map<String, SkuDTO> map = new LinkedHashMap<>();

        for (SkuRowDTO r : rows) {

            SkuDTO sku = map.computeIfAbsent(r.getSkuId(), k -> {
                SkuDTO s = new SkuDTO();
                s.setSkuId(r.getSkuId());
                s.setProductId(r.getProductId());

                // ✅ Integer → int 안전 처리
                s.setStockQuantity(
                        r.getStockQuantity() != null ? r.getStockQuantity() : 0L
                );

                return s;
            });

            // 옵션값 연결
            if (r.getOptionValueId() != null) {
                sku.getOptionValueIds().add(r.getOptionValueId());
            }
        }

        // 2) SKU 그룹핑 결과
        List<SkuDTO> skus = new ArrayList<>(map.values());

        // 3) 가격 규칙 한 번에 가져와서 skuId로 묶기
        List<SkuPriceDTO> priceRows =
                skuPriceDAO.findPriceRulesByProductId(productId);

        List<SkuPriceDTO> safePriceRows =
                (priceRows == null) ? Collections.emptyList() : priceRows;

        Map<String, List<SkuPriceDTO>> priceMap =
                safePriceRows.stream()
                        .collect(Collectors.groupingBy(
                                SkuPriceDTO::getSkuId,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));


        // 4) SKU에 가격 규칙 주입
        for (SkuDTO sku : skus) {
            List<SkuPriceDTO> rules =
                    priceMap.getOrDefault(sku.getSkuId(), Collections.emptyList());
            sku.setPriceRules(rules);
        }

        return skus;
    }
}
