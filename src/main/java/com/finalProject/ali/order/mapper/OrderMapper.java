package com.finalProject.ali.order.mapper;

import com.finalProject.ali.order.domain.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper {
    int insert(Order order);
    int updateTotalAmount(@Param("orderId") Long orderId,
                          @Param("totalAmount") Long totalAmount);
    Order findById(Long orderId);
    void updateStatus(Long orderId, String status);
}
