package com.finalProject.ali.inquiry.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SupplierLookupDAO {
    String findApprovedSupplierIdByUserId(@Param("userId") String userId);
}
