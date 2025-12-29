package com.finalProject.ali.products.service;

import com.finalProject.ali.products.dao.CategoryDAO;
import com.finalProject.ali.products.dto.CategoryDTO;
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
}
