package com.finalProject.ali.products.dao;

import com.finalProject.ali.products.dto.SkusPriceDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SkusPriceDAO {

    List<SkusPriceDTO> findPriceRulesByProductId(@Param("productId") Long productId);

    List<SkusPriceDTO> findPriceRulesBySkuId(@Param("skuId") String skuId);
}
