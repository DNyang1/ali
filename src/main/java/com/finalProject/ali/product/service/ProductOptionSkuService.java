package com.finalProject.ali.product.service;

import com.finalProject.ali.product.category.dao.CategoryDAO;
import com.finalProject.ali.product.dao.*;
import com.finalProject.ali.product.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductOptionSkuService {

    private final OptionDAO optionDAO;
    private final SkuDAO skuDAO;
    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    private final CategoryOptionTemplateDAO templateDAO;
    private final SkuPriceDAO skuPriceDAO;

    private ProductDTO loadMyProductOrThrow(Long productId, String supplierId) {
        ProductDTO product = productDAO.findById(productId, supplierId);
        if (product == null) {
            throw new IllegalArgumentException("권한 없음 또는 존재하지 않는 상품입니다. productId=" + productId);
        }
        return product;
    }

    public List<OptionDTO> optionsActive(Long productId, String supplierId) {
        loadMyProductOrThrow(productId, supplierId);
        return optionDAO.findByProductIdV2(productId);
    }


    public List<SkuDTO> skus(Long productId, String supplierId) {
        loadMyProductOrThrow(productId, supplierId);
        List<SkuDTO> list = skuDAO.findByProductId(productId);

        for (SkuDTO s : list) {
            applyDisplayStatus(s);
        }
        return list;
    }

    public boolean canEditSkuOptions(String skuId) {
        return skuDAO.countOrderItemsBySkuId(skuId) == 0;
    }

    @Transactional
    public void updateSkuOptions(Long productId, String supplierId, String skuId, List<String> optionIds) {
        loadMyProductOrThrow(productId, supplierId);

        Long skuProductId = skuDAO.findProductIdBySkuId(skuId);
        if (skuProductId == null || !skuProductId.equals(productId)) {
            throw new IllegalArgumentException("SKU가 해당 상품에 속하지 않습니다. skuId=" + skuId);
        }

        if (!canEditSkuOptions(skuId)) {
            throw new IllegalStateException("이미 주문된 SKU는 옵션을 수정할 수 없습니다.");
        }

        if (optionIds == null || optionIds.isEmpty()) {
            throw new IllegalArgumentException("옵션을 최소 1개 선택해야 합니다.");
        }

        skuDAO.deleteLinksBySkuId(skuId);
        for (String optionId : optionIds) {
            skuDAO.insertLink(skuId, optionId);
        }
    }

    public List<OptionDTO> findOptionsBySkuId(Long productId, String supplierId, String skuId) {
        loadMyProductOrThrow(productId, supplierId);
        return skuDAO.findOptionsBySkuId(skuId);
    }

    public List<SkuDTO> getSkusWithEditable(Long productId, String supplierId) {
        loadMyProductOrThrow(productId, supplierId);
        List<SkuDTO> skus = skuDAO.findByProductId(productId);

        for (SkuDTO s : skus) {
            s.setEditable(skuDAO.countOrderItemsBySkuId(s.getSkuId()) == 0);
            applyDisplayStatus(s);
        }
        return skus;
    }

    @Transactional
    public void ensureCategoryOptionsSeeded(Long productId, String supplierId) {
        ProductDTO product = loadMyProductOrThrow(productId, supplierId);

        String leafCategoryId = product.getCategoryId();
        if (leafCategoryId == null || leafCategoryId.isBlank()) return;

        List<String> chain = categoryDAO.findCategoryChain(leafCategoryId);
        if (chain == null || chain.isEmpty()) return;

        var templates = templateDAO.findTemplatesByCategoryIds(chain);
        if (templates == null || templates.isEmpty()) return;

        Map<String, CategoryOptionTemplateDAO.TemplateRow> chosen = new LinkedHashMap<>();
        for (var t : templates) {
            chosen.put(t.getOptionName(), t);
        }

        for (var t : chosen.values()) {
            String optionName = t.getOptionName();
            String optionId;

            if (optionDAO.existsProductOption(productId, optionName) > 0) {
                optionId = optionDAO.findProductOptionId(productId, optionName);
            } else {
                optionId = productId + "-" + leafCategoryId + "@" + optionName;
                optionDAO.insertProductOption(optionId, productId, leafCategoryId, optionName);
            }

            var values = templateDAO.findTemplateValueRows(t.getTemplateId());
            if (values == null) continue;

            for (var v : values) {
                String value = v.getOptionValue();
                if (value == null || value.isBlank()) continue;

                String optionValueId = optionId + "@" + value;
                optionDAO.insertProductOptionValue(optionValueId, optionId, value, v.getSortOrder());
            }
        }
    }

    @Transactional
    public String createSku(Long productId, String supplierId, SkuForm form) {
        loadMyProductOrThrow(productId, supplierId);

        String skuId = generateSkuId(productId);

        SkuDTO sku = new SkuDTO();
        sku.setSkuId(skuId);
        sku.setProductId(productId);
        sku.setStockQuantity(form.getStock() == null ? 0L : form.getStock());
        Long moq = (form.getMoq() == null ? 1L : form.getMoq());
        if (moq < 1) throw new IllegalArgumentException("MOQ는 1 이상이어야 합니다.");
        sku.setMoq(moq);
        sku.setStatus("ACTIVE");
        sku.setCreatedAt(LocalDate.now());
        if (form.getBasePrice() == null || form.getBasePrice() <= 0) {
            throw new IllegalArgumentException("기본 단가는 0보다 커야 합니다.");
        }
        skuDAO.insert(sku);
        skuPriceDAO.insertPrice(skuId, 1, null, Math.toIntExact(form.getBasePrice()));

        if (form.getRanges() != null) {
            for (Range r : form.getRanges()) {
                if (r.getMin() == null || r.getPrice() == null) continue;

                if (r.getMin() <= 1) throw new IllegalArgumentException("구간 최소수량은 2 이상이어야 합니다.");
                if (r.getMax() != null && r.getMin() > r.getMax()) throw new IllegalArgumentException("구간 범위 오류");
                if (r.getPrice() <= 0) throw new IllegalArgumentException("구간 가격은 0보다 커야 합니다.");

                skuPriceDAO.insertPrice(
                        skuId,
                        Math.toIntExact(r.getMin()),
                        (r.getMax() == null ? null : Math.toIntExact(r.getMax())),
                        Math.toIntExact(r.getPrice())
                );
            }
        }
        if (form.getOptionValueIds() != null) {
            for (String optionValueId : form.getOptionValueIds()) {
                skuDAO.insertLink(skuId, optionValueId);
            }
        }

        return skuId;
    }

    private String generateSkuId(Long productId) {
        int next = skuDAO.countByProductId(productId) + 1;
        return "P" + productId + "-SKU" + String.format("%03d", next);
    }

    @Transactional
    public void updateStatus(String skuId, String status) {
        if (!List.of("ACTIVE", "INACTIVE", "ENDED").contains(status)) {
            throw new IllegalArgumentException("허용되지 않은 상태");
        }

        var sku = skuDAO.findBySkuId(skuId);
        if (sku == null) throw new IllegalArgumentException("SKU 없음");

        if ("ACTIVE".equals(status) && sku.getStockQuantity() != null && sku.getStockQuantity() <= 0) {
            throw new IllegalStateException("재고가 0이라 판매중(ACTIVE)으로 변경 불가");
        }

        skuDAO.updateStatus(skuId, status);
    }

    private void applyDisplayStatus(SkuDTO s) {
        if ("ACTIVE".equals(s.getStatus())
                && s.getStockQuantity() != null
                && s.getStockQuantity() <= 0) {
            s.setDisplayStatus("SOLD_OUT");
        } else {
            s.setDisplayStatus(s.getStatus());
        }
    }
    @Transactional
    public void updateStock(String skuId, long stock){
        if(stock < 0){
            throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
        }
        skuDAO.updateStock(skuId, stock);
    }

    @Transactional
    public void updateOptionValue(String optionId, String optionValue){
        optionDAO.updateOptionValue(optionId, optionValue);
    }
    @Transactional
    public void toggleOptionValueStatus(Long productId, String supplierId, String optionValueId) {
        loadMyProductOrThrow(productId, supplierId);

        int links = optionDAO.countSkuLinksByOptionValueId(optionValueId);
        if (links > 0) {
            throw new IllegalStateException("SKU가 등록된 옵션값은 비활성화할 수 없습니다.");
        }

        String cur = optionDAO.findOptionValueStatus(optionValueId);
        if (cur == null) throw new IllegalArgumentException("옵션값이 존재하지 않습니다.");

        String next = "ACTIVE".equals(cur) ? "INACTIVE" : "ACTIVE";
        optionDAO.updateOptionValueStatus(optionValueId, next);
    }
    public List<OptionDTO> optionsAll(Long productId, String supplierId) {
        loadMyProductOrThrow(productId, supplierId);
        return optionDAO.findByProductIdV2All(productId);

    }



}

