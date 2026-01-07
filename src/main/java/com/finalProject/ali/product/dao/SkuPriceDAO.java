package com.finalProject.ali.product.dao;

import com.finalProject.ali.product.dto.SkuPriceDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SkuPriceDAO {
    //태민
    int deleteAllBySkuId(@Param("skuId") String skuId);
    void insertPrice(@Param("skuId") String skuId,
                     @Param("minQty") Integer minQty,
                     @Param("maxQty") Integer maxQty,
                     @Param("price") Integer price);
    List<SkuPriceDTO> findBySkuId(@Param("skuId") String skuId);
    //현성
    List<SkuPriceDTO> findPriceRulesByProductId(@Param("productId") Long productId);
    List<SkuPriceDTO> findPriceRulesBySkuId(@Param("skuId") String skuId);
    Long findUnitPriceByQty(@Param("skuId") String skuId, @Param("qty") Long qty);
    SkuPriceDTO findPriceBySkuAndQty(
            @Param("skuId") String skuId,
            @Param("qty") Long qty
    );

}
