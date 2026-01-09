package com.finalProject.ali.order.mapper;

import com.finalProject.ali.order.domain.OrderItem;
import com.finalProject.ali.order.dto.OrderItemResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderItemMapper {
    int insert(OrderItem item);
    List<OrderItemResponse> findByOrderId(Long orderId);

}
