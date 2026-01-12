package com.finalProject.ali.mypage.supplier.inquiry.dao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InquiryStatusDAO {

    int updateToInProgress(@Param("inquiryId") Long inquiryId);
    Integer findStatus(@Param("inquiryId") Long inquiryId);
}
