package com.finalProject.ali.mypage.supplier.product.service;

import com.finalProject.ali.mypage.supplier.product.dao.OptionDAO;
import com.finalProject.ali.mypage.supplier.product.dao.SkuDAO;
import com.finalProject.ali.mypage.supplier.product.dto.OptionDTO;
import com.finalProject.ali.mypage.supplier.product.dto.SkuDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductOptionSkuService {

    private final OptionDAO optionDAO;
    private final SkuDAO skuDAO;

    public java.util.List<OptionDTO> options(Long productId) {
        return optionDAO.findByProductId(productId);
    }

    public void addOption(Long productId, OptionDTO form) {
        form.setOptionId(UUID.randomUUID().toString());
        form.setProductId(productId);
        optionDAO.insert(form);
    }

    public java.util.List<SkuDTO> skus(Long productId) {
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
    public void updateSkuOptions(String skuId, java.util.List<String> optionIds) {
        skuDAO.deleteLinksBySkuId(skuId);
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
    public java.util.List<OptionDTO> findOptionsBySkuId(String skuId) {
        return skuDAO.findOptionsBySkuId(skuId);
    }
    public List<SkuDTO> getSkusWithEditable(Long productId) {
        List<SkuDTO> skus = skuDAO.findByProductId(productId);

        for (SkuDTO s : skus) {
            boolean editable = skuDAO.countOrderItemsBySkuId(s.getSkuId()) == 0;
            s.setEditable(editable);
        }
        return skus;
    }
}
