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

        List<SkuRowDTO> rows = skuDAO.findSkuRowsByProductId(productId);

        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        Map<String, SkuDTO> map = new LinkedHashMap<>();

        for (SkuRowDTO r : rows) {

            SkuDTO sku = map.computeIfAbsent(r.getSkuId(), k -> {
                SkuDTO s = new SkuDTO();
                s.setSkuId(r.getSkuId());
                s.setProductId(r.getProductId());

                s.setStockQuantity(
                        r.getStockQuantity() != null ? r.getStockQuantity() : 0L
                );

                return s;
            });

            if (r.getOptionValueId() != null) {
                sku.getOptionValueIds().add(r.getOptionValueId());
            }
        }

        List<SkuDTO> skus = new ArrayList<>(map.values());

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


        for (SkuDTO sku : skus) {
            List<SkuPriceDTO> rules =
                    priceMap.getOrDefault(sku.getSkuId(), Collections.emptyList());
            sku.setPriceRules(rules);
        }

        return skus;
    }

    @Override
    public SkuDTO findSkuByOptionValues(List<String> optionValueIds) {

        if (optionValueIds == null || optionValueIds.isEmpty()) {
            return null;
        }

        return skuDAO.findSkuByOptionValues(
                optionValueIds,
                optionValueIds.size()
        );
    }

    @Override
    public List<SkuDTO> getProductDetailSkus(Long productId) {

        List<SkuDTO> skus =
                skuDAO.findProductDetailSkus(productId);

        for (SkuDTO sku : skus) {
            sku.setPriceRules(
                    skuPriceDAO.findBySkuId(sku.getSkuId())
            );
        }

        return skus;
    }

}
