package com.finalProject.ali.product.category.controller;

import com.finalProject.ali.product.category.dto.CategoryDTO;
import com.finalProject.ali.product.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryApiController {

    private final CategoryService categoryService;

    @GetMapping("/main")
    public List<CategoryDTO> mainCategories(){
        return categoryService.getMainCategories();
    }

    @GetMapping("/children")
    public List<CategoryDTO> getChildren(@RequestParam String parentId) {
        return categoryService.getChildren(parentId);
    }


}
