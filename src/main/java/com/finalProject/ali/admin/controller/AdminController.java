package com.finalProject.ali.admin.controller;

import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.service.UserService; // UserService 임포트
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; // 어노테이션 추가
import org.springframework.ui.Model; // 올바른 Model 클래스 임포트
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller // 1. 컨트롤러 어노테이션 추가
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService; // 2. SupplierService 대신 UserService 주입

    @GetMapping("/adminpage")
    public String adminpage(){
        return "admin/adminpage";
    }
    // 1. 공급자 입점 신청 대기 목록 조회
    @GetMapping("/supplier/list")
    public String supplierList(Model model) {
        // UserService에 정의된 메서드 호출
        List<SupplierDTO> pendingList = userService.getPendingSuppliers();
        model.addAttribute("suppliers", pendingList);
        return "admin/supplierList";
    }

    // 2. 공급자 입점 승인 처리 (추가 권장)
    @PostMapping("/supplier/approve")
    public String approveSupplier(@RequestParam("supplierId") String supplierId,
                                  @RequestParam("userId") String userId)
    {
        // UserService에 이미 구현된 approveSupplier 호출
        userService.approveSupplier(supplierId, userId);
        return "redirect:/admin/supplier/list";
    }
}