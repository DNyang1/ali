package com.finalProject.ali.pricing;


import com.finalProject.ali.product.dao.SkuDAO;
import com.finalProject.ali.product.dao.SkuPriceDAO;
import com.finalProject.ali.product.dto.EventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final SkuPriceDAO skuPriceDAO;
    private final SkuDAO skuDAO;

    public long getFinalUnitPrice(String skuId, long quantity) {
        long basePrice = skuPriceDAO.findUnitPriceByQty(skuId, quantity);
        EventDTO event = skuDAO.findActiveEvent("SKU", skuId);
        if (event == null) {
            Long productId = skuDAO.findProductIdBySkuId(skuId);
            if (productId != null) {
                event = skuDAO.findActiveEvent("PRODUCT", String.valueOf(productId));
            }
        }

        if (event != null) {
            if ("RATE".equals(event.getDiscountType())) {
                return basePrice * (100 - event.getDiscountValue()) / 100;
            }
            if ("AMOUNT".equals(event.getDiscountType())) {
                return Math.max(0, basePrice - event.getDiscountValue());
            }
        }

        return basePrice;
    }
}
