package com.finalProject.ali.checkout.service;

import com.finalProject.ali.checkout.dto.CheckoutItem;
import com.finalProject.ali.checkout.dto.CheckoutRequest;
import com.finalProject.ali.checkout.dto.CheckoutResponse;
import com.finalProject.ali.checkout.mapper.CheckoutMapper;
import com.finalProject.ali.product.dao.SkuPriceDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService{
    private final SkuPriceDAO skuPriceDAO;
    private final CheckoutMapper checkoutMapper;

    @Override
    public CheckoutResponse checkout(String userId, CheckoutRequest request) {
        List<CheckoutItem> resultItems = new ArrayList<>();
        long totalAmount = 0L;

        for (CheckoutRequest.CheckoutItemRequest itemReq : request.getItems()) {

            String skuId = itemReq.getSkuId();
            Long quantity = itemReq.getQuantity();

            Long unitPrice =
                    skuPriceDAO.findUnitPriceByQty(skuId, quantity);

            long lineAmount = unitPrice * quantity;

            List<CheckoutItem> list = checkoutMapper.findItemInfo(skuId);
            CheckoutItem item = list.get(0);
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setLineAmount(lineAmount);

            resultItems.add(item);
            totalAmount += lineAmount;
        }

        return new CheckoutResponse(
                resultItems,
                totalAmount,
                request.getAddressId()
        );
    }
}
