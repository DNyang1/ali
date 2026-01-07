package com.finalProject.ali.checkout.mapper;

import com.finalProject.ali.checkout.dto.CheckoutItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CheckoutMapper {
    List<CheckoutItem> findItemInfo(@Param("skuId") String skuId);
}
