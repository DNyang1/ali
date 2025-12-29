package com.finalProject.ali.products.dao;

import com.finalProject.ali.products.dto.CategoryDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryDAO {
    List<CategoryDTO> selectMainCategories();
}
