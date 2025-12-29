package com.finalProject.ali.supplier_mypage.product.dao;

import com.finalProject.ali.supplier_mypage.product.dto.ProductDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ProductDAO {
    List<ProductDTO> findBySupplierId(String supplierId);
    void insert(ProductDTO product);
    ProductDTO findById(Long productId);
    void update(ProductDTO product);

    int updateStatus(@Param("productId") Long productId,
                     @Param("status") String status,
                     @Param("updatedAt") LocalDate updatedAt);
}
