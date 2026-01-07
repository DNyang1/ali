package com.finalProject.ali.product.dao;

import com.finalProject.ali.product.category.dto.CategoryDTO;
import com.finalProject.ali.product.dto.ProductDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ProductDAO {
    //태민
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

    // 현성
    List<ProductDTO> productList();
    ProductDTO productDetail(Long productId);
    List<ProductDTO> selectByCustom(@Param("isCustomizable") Long isCustomizable);
    List<ProductDTO> selectByRootCategory(
            @Param("categoryId") String categoryId);
    List<ProductDTO> selectByCategoryAndCustom(
            String categoryId,
            Long isCustomizable
    );
    List<CategoryDTO> selectRootCategories();
    List<ProductDTO> searchByKeyword(String keyword);
    List<ProductDTO> findByCategoryId(String categoryId);

    String findSupplierIdByProductId(@Param("productId") Long productId);
}
