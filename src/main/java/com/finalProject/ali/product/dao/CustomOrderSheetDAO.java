package com.finalProject.ali.product.dao;

import com.finalProject.ali.product.dto.CustomOrderSheetDTO;
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
    CustomOrderSheetDTO findByInquiryId(@Param("inquiryId") Long inquiryId);

    CustomOrderSheetDTO findById(@Param("sheetId") Long sheetId);

    int updateStatus(@Param("sheetId") Long sheetId,
                     @Param("status") String status);
    int updateContent(@Param("sheetId") Long sheetId,
                      @Param("quantity") Long quantity,
                      @Param("unitPrice") Long unitPrice,
                      @Param("optionsText") String optionsText);

    int updateContentByInquiryId(@Param("inquiryId") Long inquiryId,
                                 @Param("quantity") Long quantity,
                                 @Param("unitPrice") Long unitPrice,
                                 @Param("optionsText") String optionsText);

}