package com.finalProject.ali.supplier_mypage.product.dao;

import com.finalProject.ali.supplier_mypage.product.dto.OptionDTO;
import com.finalProject.ali.supplier_mypage.product.dto.SkuDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SkuDAO {
    void insert(SkuDTO sku);

    List<SkuDTO> findByProductId(@Param("productId") Long productId);

    void linkOption(@Param("optionId") String optionId,
                    @Param("skuId") String skuId);

    List<OptionDTO> findOptionsBySkuId(@Param("skuId") String skuId);

    void deleteLinksBySkuId(@Param("skuId") String skuId);

    void insertLink(@Param("skuId") String skuId, @Param("optionId") String optionId);

    int countOrderItemsBySkuId(@Param("skuId") String skuId);

}
