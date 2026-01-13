package com.finalProject.ali.mypage.supplier.inquiry.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SupplierInquiryDAO {

    long countBySupplierAndStatus(@Param("supplierId") String supplierId,
                                  @Param("status") int status);
}
