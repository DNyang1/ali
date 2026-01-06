package com.finalProject.ali.product.category.dao;

import com.finalProject.ali.product.category.dto.SupplierCategoryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SupplierCategoryDAO {
    List<SupplierCategoryDTO> findRoot();
    List<SupplierCategoryDTO> findChildren(@Param("parentId") String parentId);
    SupplierCategoryDTO findById(@Param("categoryId") String categoryId);
    List<String> findCategoryChain(@Param("categoryId") String categoryId);
}

