package com.finalProject.ali.product.category.controller;

import com.finalProject.ali.product.category.dao.SupplierCategoryDAO;
import com.finalProject.ali.product.category.dto.SupplierCategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage/supplier/category")
public class SupplierCategoryApiController {

    private final SupplierCategoryDAO categoryDAO;

    @GetMapping("/children")
    public List<SupplierCategoryDTO> children(@RequestParam String parentId) {
        return categoryDAO.findChildren(parentId);
    }
}
