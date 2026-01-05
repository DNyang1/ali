package com.finalProject.ali.mypage.supplier.product.service;

import com.finalProject.ali.mypage.supplier.product.dao.SkuPriceDAO;
import com.finalProject.ali.mypage.supplier.product.dto.SkuPriceRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SkuPriceService {

    private final SkuPriceDAO skuPriceDAO;

    @Transactional
    public void replacePrices(String skuId, SkuPriceRequestDTO req) {

        if (req == null || req.getBasePrice() == null || req.getBasePrice() <= 0) {
            throw new IllegalArgumentException("기본가가 올바르지 않습니다.");
        }

        // 1. 기존 가격 전부 삭제
        skuPriceDAO.deleteAllBySkuId(skuId);

        // 2. 기본가 (1~∞)
        skuPriceDAO.insertPrice(skuId, 1, null, req.getBasePrice());

        // 3. 구간가
        if (req.getRanges() == null) return;

        List<SkuPriceRequestDTO.RangeDTO> ranges = req.getRanges();
        ranges.sort(Comparator.comparingInt(SkuPriceRequestDTO.RangeDTO::getMinQty));

        int prevMax = 0;
        for (SkuPriceRequestDTO.RangeDTO r : ranges) {

            if (r.getMinQty() == null || r.getMaxQty() == null || r.getPrice() == null)
                throw new IllegalArgumentException("구간 값 누락");

            if (r.getMinQty() < 1 || r.getMaxQty() < 1 || r.getMinQty() > r.getMaxQty())
                throw new IllegalArgumentException("구간 범위 오류");

            if (r.getPrice() <= 0)
                throw new IllegalArgumentException("구간 가격 오류");

            if (r.getMinQty() <= prevMax)
                throw new IllegalArgumentException("구간 겹침");

            prevMax = r.getMaxQty();

            skuPriceDAO.insertPrice(skuId, r.getMinQty(), r.getMaxQty(), r.getPrice());
        }
    }
}
