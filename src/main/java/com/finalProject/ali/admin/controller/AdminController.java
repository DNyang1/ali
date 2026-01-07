package com.finalProject.ali.admin.controller;

import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
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

    // 공급자 입점 승인 처리
    @PostMapping("/supplier/approve")
    public String approveSupplier(@RequestParam("supplierId") String supplierId,
                                  @RequestParam("userId") String userId)
    {
        // UserService에 이미 구현된 approveSupplier 호출
        userService.approveSupplier(supplierId, userId);
        return "redirect:/admin/supplier/list";
    }
    // 공급자 입점 반려 처리
    @PostMapping("/supplier/reject")
    public String rejectSupplier(@RequestParam("supplierId") String supplierId,
                                 @RequestParam("memo") String memo) {
        // 상태를 REJECTED로 변경하는 서비스 호출
        userService.updateSupplierStatus(supplierId, "REJECTED", memo);
        return "redirect:/admin/supplier/list";
    }

    // 1. 회원 관리 페이지 이동
    @GetMapping("/users")
    public String userList(Model model) {
        List<UserDTO> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("activeMenu", "users"); // 사이드바 활성화용
        return "admin/userList";
    }
    // 2. 계정 상태 변경 (AJAX 또는 Form)
    @PostMapping("/users/status")
    public String updateUserStatus(@RequestParam("userId") String userId,
                                   @RequestParam("status") String status) {
        userService.updateUserStatus(userId, status);
        return "redirect:/admin/users";
    }
    // 3. 권한 변경
    @PostMapping("/users/role")
    public String updateUserRole(@RequestParam("userId") String userId,
                                 @RequestParam("role") String role) {
        userService.changeUserRole(userId, role);
        return "redirect:/admin/users";
    }



}