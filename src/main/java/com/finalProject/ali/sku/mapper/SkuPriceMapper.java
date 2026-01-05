package com.finalProject.ali.sku.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SkuPriceMapper {

    Long findUnitPriceByQty(
            @Param("skuId") String skuId,
            @Param("qty") Long qty
    );
}
