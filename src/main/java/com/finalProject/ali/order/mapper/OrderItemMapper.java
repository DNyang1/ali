package com.finalProject.ali.order.mapper;

import com.finalProject.ali.order.domain.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper {
    int insert(OrderItem item);
}
