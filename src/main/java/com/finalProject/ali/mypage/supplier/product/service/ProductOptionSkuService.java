package com.finalProject.ali.mypage.supplier.product.service;

import com.finalProject.ali.mypage.supplier.category.dao.SupplierCategoryDAO;
import com.finalProject.ali.mypage.supplier.product.dao.*;
import com.finalProject.ali.mypage.supplier.product.dto.*;
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
    private final SupplierCategoryDAO categoryDAO;
    private final CategoryOptionTemplateDAO templateDAO;
    private final SkuPriceDAO skuPriceDAO;

    private ProductDTO loadMyProductOrThrow(Long productId, String supplierId) {
        ProductDTO product = productDAO.findById(productId, supplierId);
        if (product == null) {
            throw new IllegalArgumentException("권한 없음 또는 존재하지 않는 상품입니다. productId=" + productId);
        }
        return product;
    }

    public List<OptionDTO> options(Long productId, String supplierId) {
        loadMyProductOrThrow(productId, supplierId);
        return optionDAO.findByProductId(productId);
    }

    @Transactional
    public void addOption(Long productId, String supplierId, OptionDTO form) {
        ProductDTO product = loadMyProductOrThrow(productId, supplierId);

        String categoryId = product.getCategoryId();
        String optionName = form.getOptionName();
        String optionValue = form.getOptionValue();

        String optionId;
        if (optionDAO.existsProductOption(productId, optionName) > 0) {
            optionId = optionDAO.findProductOptionId(productId, optionName);
        } else {
            optionId = productId + "-" + categoryId + "@" + optionName;
            optionDAO.insertProductOption(optionId, productId, categoryId, optionName);
        }

        String optionValueId = optionId + "@" + optionValue;
        optionDAO.insertProductOptionValue(optionValueId, optionId, optionValue, 0);
    }

    public List<SkuDTO> skus(Long productId, String supplierId) {
        loadMyProductOrThrow(productId, supplierId);
        return skuDAO.findByProductId(productId);
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
        sku.setStockQuantity(form.getStock());
        sku.setCreatedAt(LocalDate.now());
        skuDAO.insert(sku);

        skuPriceDAO.insertPrice(skuId, 1L, null, form.getBasePrice());

        if (form.getRanges() != null) {
            for (Range r : form.getRanges()) {
                skuPriceDAO.insertPrice(skuId, r.getMin(), r.getMax(), r.getPrice());
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
}

