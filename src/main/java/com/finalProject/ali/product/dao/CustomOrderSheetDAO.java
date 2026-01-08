package com.finalProject.ali.product.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CustomOrderSheetDAO {

    int insert(@Param("productId") Long productId,
               @Param("inquiryId") Long inquiryId,
               @Param("skuId") String skuId,
               @Param("quantity") Long quantity,
               @Param("unitPrice") Long unitPrice,
               @Param("optionsText") String optionsText,
               @Param("status") String status);
}