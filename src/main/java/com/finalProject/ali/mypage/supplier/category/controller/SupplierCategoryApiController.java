package com.finalProject.ali.mypage.supplier.category.controller;

import com.finalProject.ali.mypage.supplier.category.dao.SupplierCategoryDAO;
import com.finalProject.ali.mypage.supplier.category.dto.SupplierCategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
