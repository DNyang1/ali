package com.finalProject.ali.mypage.supplier.order.service;

import com.finalProject.ali.mypage.supplier.order.dao.SupplierOrderItemDAO;
import com.finalProject.ali.mypage.supplier.order.dto.ShippingItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierShippingService {

    private final SupplierOrderItemDAO orderItemDAO;

    public long countReady(String supplierId) {
        return orderItemDAO.countShippingReady(supplierId);
    }

    public List<ShippingItemDTO> listReadyItems(String supplierId) {
        return orderItemDAO.findShippingReadyItems(supplierId);
    }

    @Transactional
    public void ship(Long orderItemId, String carrier, String trackingNo) {
        if (carrier == null || carrier.isBlank()) throw new IllegalArgumentException("택배사를 선택하세요.");
        if (trackingNo == null || trackingNo.isBlank()) throw new IllegalArgumentException("송장번호를 입력하세요.");

        orderItemDAO.updateShippingInfo(orderItemId, carrier.trim(), trackingNo.trim());
        orderItemDAO.updateSupplierStatus(orderItemId, "SHIPPED");
    }
}
