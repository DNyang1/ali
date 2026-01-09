package com.finalProject.ali.product.category.service;

import com.finalProject.ali.product.category.dao.CategoryDAO;
import com.finalProject.ali.product.category.dto.CategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryDAO categoryDAO;

    public List<CategoryDTO> getMainCategories() {
        return categoryDAO.selectMainCategories();
    }

    public List<CategoryDTO> getChildren(String parentId) {
        return categoryDAO.findByParentId(parentId);
    }


}
