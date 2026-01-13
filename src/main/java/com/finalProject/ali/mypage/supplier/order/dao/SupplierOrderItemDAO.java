package com.finalProject.ali.mypage.supplier.order.dao;

import com.finalProject.ali.mypage.supplier.order.dto.ShippingItemDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SupplierOrderItemDAO {

    long countShippingReady(@Param("supplierId") String supplierId);

    List<ShippingItemDTO> findShippingReadyItems(@Param("supplierId") String supplierId);

    int updateShippingInfo(@Param("orderItemId") Long orderItemId,
                           @Param("carrier") String carrier,
                           @Param("trackingNo") String trackingNo);

    int updateSupplierStatus(@Param("orderItemId") Long orderItemId,
                             @Param("supplierStatus") String supplierStatus);
}
