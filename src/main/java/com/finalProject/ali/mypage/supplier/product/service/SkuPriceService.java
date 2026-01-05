package com.finalProject.ali.mypage.supplier.product.service;

import com.finalProject.ali.mypage.supplier.product.dao.SkuDAO;
import com.finalProject.ali.mypage.supplier.product.dao.SkuPriceDAO;
import com.finalProject.ali.mypage.supplier.product.dto.SkuPriceRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;


    @Service
    @RequiredArgsConstructor
    public class SkuPriceService {

        private final SkuPriceDAO skuPriceDAO;
        private final SkuDAO skuDAO;

        @Transactional
        public void replacePricesAndMoq(String skuId, SkuPriceRequestDTO req) {

            if (req.getMoq() != null) {
                long moq = req.getMoq();
                if (moq < 1) throw new IllegalArgumentException("MOQ는 1 이상이어야 합니다.");



                skuDAO.updateMoq(skuId, moq);
            }

            if (req.getBasePrice() == null || req.getBasePrice() <= 0) {
                throw new IllegalArgumentException("기본가가 올바르지 않습니다.");
            }

            skuPriceDAO.deleteAllBySkuId(skuId);

            skuPriceDAO.insertPrice(skuId, 1, null, req.getBasePrice());

            if (req.getRanges() == null) return;

            var ranges = req.getRanges();
            ranges.sort(Comparator.comparingInt(SkuPriceRequestDTO.RangeDTO::getMinQty));

            int prevMax = 0;
            for (var r : ranges) {
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

