package com.finalProject.ali.product.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SkuStockDAO {
    int deductOnShip(@Param("orderItemId") Long orderItemId);

    Long getStockQuantity(@Param("skuId") String skuId);

    int deductStock(@Param("skuId") String skuId, @Param("quantity") Long quantity);
}
