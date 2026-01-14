package com.finalProject.ali.admin.controller;

import com.finalProject.ali.admin.dto.AdminDashboardDTO;
import com.finalProject.ali.repository.QnaRepository;
import com.finalProject.ali.user.dao.UserDAO;
import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import com.finalProject.ali.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDAO userDAO; // 👈 회원 통계 데이터용 (MyBatis)

    @Autowired
    private QnaRepository qnaRepository; // 👈 QnA 개수 확인용 (JPA)

    // 대시보드 페이지 (DTO 조립)
    @GetMapping("/adminpage")
    public String adminpage(Model model) {

        // 1. DTO 조립 (Builder 패턴 사용)
        AdminDashboardDTO dashboard = AdminDashboardDTO.builder()
                .totalUsers(userDAO.countAllUsers())
                .todayUsers(userDAO.countTodayUsers())
                .pendingSuppliers(userDAO.countPendingSuppliers())
                .waitingQna(qnaRepository.findByStatusOrderByCreatedAtDesc("WAITING").size())
                .build();

        // 2. 완성된 DTO를 화면으로 전송
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("pageTitle", "관리자 대시보드");

        return "admin/adminpage";
    }

    // 1. 공급자 입점 신청 대기 목록
    @GetMapping("/supplier/list")
    public String supplierList(Model model) {
        List<SupplierDTO> pendingList = userService.getPendingSuppliers();
        model.addAttribute("suppliers", pendingList);
        return "admin/supplierList";
    }

    // 공급자 승인
    @PostMapping("/supplier/approve")
    public String approveSupplier(@RequestParam("supplierId") String supplierId,
                                  @RequestParam("userId") String userId) {
        userService.approveSupplier(supplierId, userId);
        return "redirect:/admin/supplier/list";
    }

    // 공급자 반려
    @PostMapping("/supplier/reject")
    public String rejectSupplier(@RequestParam("supplierId") String supplierId,
                                 @RequestParam("memo") String memo) {
        userService.updateSupplierStatus(supplierId, "REJECTED", memo);
        return "redirect:/admin/supplier/list";
    }

    // 2. 회원 관리 페이지
    @GetMapping("/users")
    public String userList(Model model) {
        List<UserDTO> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("activeMenu", "users");
        return "admin/userList";
    }

    // 계정 상태 변경
    @PostMapping("/users/status")
    public String updateUserStatus(@RequestParam("userId") String userId,
                                   @RequestParam("status") String status,
                                   @RequestParam(value = "reason", required = false) String reason) {
        userService.updateUserStatus(userId, status, reason);
        return "redirect:/admin/users";
    }

    // 권한 변경
    @PostMapping("/users/role")
    public String updateUserRole(@RequestParam("userId") String userId,
                                 @RequestParam("role") String role) {
        userService.setAuthority(userId, role);
        return "redirect:/admin/users";
    }
}