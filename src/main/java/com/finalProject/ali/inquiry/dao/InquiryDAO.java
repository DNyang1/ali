package com.finalProject.ali.inquiry.dao;

import com.finalProject.ali.inquiry.dto.InquiryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InquiryDAO {
    int insertInquiry(InquiryDTO dto);

    List<InquiryDTO> findMyInquiries(@Param("userId") String userId);
    List<InquiryDTO> findMyInquiriesByStatus(@Param("userId") String userId,
                                             @Param("status") int status);

    InquiryDTO findById(@Param("inquiryId") Long inquiryId);

    int updateStatus(@Param("inquiryId") Long inquiryId, @Param("status") Long status);

    long countBySupplierAndStatus(@Param("supplierId") String supplierId, @Param("status") Long status);

    List<InquiryDTO> findBySupplierAndStatus(@Param("supplierId") String supplierId, @Param("status") Long status);

    List<InquiryDTO> findBySupplierAll(@Param("supplierId") String supplierId);
}

