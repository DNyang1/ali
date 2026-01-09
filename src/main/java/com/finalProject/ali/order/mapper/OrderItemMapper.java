package com.finalProject.ali.order.mapper;

import com.finalProject.ali.order.domain.OrderItem;
import com.finalProject.ali.order.dto.OrderItemResponse;
import com.finalProject.ali.order.dto.SupplierOrderItemDetailResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderItemMapper {
    int insert(OrderItem item);
    List<OrderItemResponse> findByOrderId(Long orderId);

     SupplierOrderItemDetailResponse findSupplierOrderItemDetail(
             Long orderItemId, String supplierId);

     int updateSupplierShipping(Long orderItemId,
                                String supplierId,
                                String carrier,
                                String trackingNo,
                                String status
     );
}
