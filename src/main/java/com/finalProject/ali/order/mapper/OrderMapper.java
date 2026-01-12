package com.finalProject.ali.order.mapper;

import com.finalProject.ali.order.domain.Order;
import com.finalProject.ali.order.dto.OrderSummaryResponse;
import com.finalProject.ali.order.dto.SupplierOrderItemResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    int insert(Order order);
    int updateTotalAmount(@Param("orderId") Long orderId,
                          @Param("totalAmount") Long totalAmount);
    Order findById(Long orderId);
    void updateStatus(Long orderId, String status);
    List<OrderSummaryResponse> findMyOrders(String userId);
    Order findByIdAndUser(Long orderId, String userId);
    List<SupplierOrderItemResponse> findSupplierOrderItems(String supplierId);

}
