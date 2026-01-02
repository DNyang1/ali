package com.finalProject.ali.mypage.supplier.product.dao;

import com.finalProject.ali.mypage.supplier.product.dto.SkuPriceDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SkuPriceDAO {

    void insertPrice(@Param("skuId") String skuId,
                     @Param("minQty") Long minQty,
                     @Param("maxQty") Long maxQty,
                     @Param("price") Long price);
    List<SkuPriceDTO> findBySkuId(@Param("skuId") String skuId);
}
