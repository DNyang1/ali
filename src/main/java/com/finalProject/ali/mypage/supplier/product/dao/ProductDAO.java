package com.finalProject.ali.mypage.supplier.product.dao;

import com.finalProject.ali.mypage.supplier.product.dto.ProductDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ProductDAO {

    List<ProductDTO> findBySupplierId(@Param("supplierId") String supplierId);

    void insert(ProductDTO product);

    ProductDTO findById(@Param("productId") Long productId,
                        @Param("supplierId") String supplierId);

    int update(@Param("product") ProductDTO product,
               @Param("supplierId") String supplierId);

    int updateStatus(@Param("productId") Long productId,
                     @Param("supplierId") String supplierId,
                     @Param("status") String status,
                     @Param("updatedAt") LocalDate updatedAt);
}
