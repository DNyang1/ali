package com.finalProject.ali.products.controller;

import com.finalProject.ali.products.dto.CategoryDTO;
import com.finalProject.ali.products.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryRestController {

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
