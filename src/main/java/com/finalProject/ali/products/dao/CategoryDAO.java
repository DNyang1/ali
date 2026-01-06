package com.finalProject.ali.products.dao;

import com.finalProject.ali.products.dto.CategoryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryDAO {
    List<CategoryDTO> selectMainCategories();

    @Select("""
        SELECT category_id AS categoryId,
               category_name AS categoryName
        FROM category
        WHERE parent_id = #{parentId}
        ORDER BY category_id
    """)
    List<CategoryDTO> findByParentId(String parentId);
}
