package com.finalProject.ali.products.dao;

import com.finalProject.ali.products.dto.CategoryDTO;
import com.finalProject.ali.products.dto.ProductsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductsDAO {

    List<ProductsDTO> productList();

    ProductsDTO productDetail(Long productId);

    List<ProductsDTO> selectByCustom(@Param("isCustomizable") Long isCustomizable);

    List<ProductsDTO> selectByRootCategory(
            @Param("categoryId") String categoryId);

    List<ProductsDTO> selectByCategoryAndCustom(
            String categoryId,
            Long isCustomizable
    );

    List<CategoryDTO> selectRootCategories();

    List<ProductsDTO> searchByKeyword(String keyword);

    List<ProductsDTO> findByCategoryId(String categoryId);
}
