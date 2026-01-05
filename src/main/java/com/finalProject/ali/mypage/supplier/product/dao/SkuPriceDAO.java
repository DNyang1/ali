package com.finalProject.ali.mypage.supplier.product.dao;

import com.finalProject.ali.mypage.supplier.product.dto.SkuPriceDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SkuPriceDAO {

    int deleteAllBySkuId(@Param("skuId") String skuId);

    void insertPrice(@Param("skuId") String skuId,
                     @Param("minQty") Integer minQty,
                     @Param("maxQty") Integer maxQty,
                     @Param("price") Integer price);

    List<SkuPriceDTO> findBySkuId(@Param("skuId") String skuId);
}
