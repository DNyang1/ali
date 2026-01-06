package com.finalProject.ali.product.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface SupplierDAO {
    String findSupplierIdByUserId(@Param("userId") String userId);
}
