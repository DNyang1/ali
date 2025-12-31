package com.finalProject.ali.mypage.supplier.product.service;

import com.finalProject.ali.mypage.supplier.category.dao.SupplierCategoryDAO;
import com.finalProject.ali.mypage.supplier.product.dao.CategoryOptionTemplateDAO;
import com.finalProject.ali.mypage.supplier.product.dao.OptionDAO;
import com.finalProject.ali.mypage.supplier.product.dao.ProductDAO;
import com.finalProject.ali.mypage.supplier.product.dao.SkuDAO;
import com.finalProject.ali.mypage.supplier.product.dto.OptionDTO;
import com.finalProject.ali.mypage.supplier.product.dto.ProductDTO;
import com.finalProject.ali.mypage.supplier.product.dto.SkuDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductOptionSkuService {

    private final OptionDAO optionDAO;
    private final SkuDAO skuDAO;
    private final ProductDAO productDAO;
    private final SupplierCategoryDAO categoryDAO;
    private final CategoryOptionTemplateDAO templateDAO;

    public List<OptionDTO> options(Long productId) {
        return optionDAO.findByProductId(productId);
    }

    @Transactional
    public void addOption(Long productId, OptionDTO form) {
        ProductDTO product = productDAO.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다. productId=" + productId);
        }

        String categoryId = product.getCategoryId(); // ProductDTO에 categoryId 존재
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
        optionDAO.insertProductOptionValue(optionValueId, optionId, optionValue,0);
    }

    public List<SkuDTO> skus(Long productId) {
        return skuDAO.findByProductId(productId);
    }

    public String addSku(SkuDTO form) {
        form.setSkuId(UUID.randomUUID().toString());
        form.setCreatedAt(LocalDate.now());
        skuDAO.insert(form);
        return form.getSkuId();
    }

    public void link(String skuId, String optionId) {
        skuDAO.linkOption(optionId, skuId);
    }

    public boolean canEditSkuOptions(String skuId) {
        return skuDAO.countOrderItemsBySkuId(skuId) == 0;
    }

    @Transactional
    public void updateSkuOptions(String skuId, List<String> optionIds) {
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

    public List<OptionDTO> findOptionsBySkuId(String skuId) {
        return skuDAO.findOptionsBySkuId(skuId);
    }

    public List<SkuDTO> getSkusWithEditable(Long productId) {
        List<SkuDTO> skus = skuDAO.findByProductId(productId);
        for (SkuDTO s : skus) {
            s.setEditable(skuDAO.countOrderItemsBySkuId(s.getSkuId()) == 0);
        }
        return skus;
    }

    @Transactional
    public void ensureCategoryOptionsSeeded(Long productId) {

        ProductDTO product = productDAO.findById(productId);
        if (product == null) return;

        String leafCategoryId = product.getCategoryId();
        if (leafCategoryId == null || leafCategoryId.isBlank()) return;

        // 1) category chain (root -> leaf)
        List<String> chain = categoryDAO.findCategoryChain(leafCategoryId);
        if (chain == null || chain.isEmpty()) return;

        // 2) all templates in chain
        var templates = templateDAO.findTemplatesByCategoryIds(chain);
        if (templates == null || templates.isEmpty()) return;

        // 3) merge (child overrides parent)
        Map<String, CategoryOptionTemplateDAO.TemplateRow> chosen = new LinkedHashMap<>();
        for (var t : templates) {
            chosen.put(t.getOptionName(), t); // later(=child) overrides
        }

        // 4) seed
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


}
