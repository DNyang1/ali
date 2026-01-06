package com.finalProject.ali.product.category.dao;

import com.finalProject.ali.product.category.dto.CategoryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryDAO {
    List<CategoryDTO> findRoot();
    List<CategoryDTO> findChildren(@Param("parentId") String parentId);
    CategoryDTO findById(@Param("categoryId") String categoryId);
    List<String> findCategoryChain(@Param("categoryId") String categoryId);
    @Select("""
        SELECT category_id AS categoryId,
               category_name AS categoryName
        FROM category
        WHERE parent_id = #{parentId}
        ORDER BY category_id
    """)
    List<CategoryDTO> findByParentId(String parentId);
    List<CategoryDTO> selectMainCategories();

}

