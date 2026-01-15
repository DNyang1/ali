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
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("activeMenu", "suppliers");
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
    // 2. 회원 관리 페이지 (필터 + 페이징 적용)
    @GetMapping("/users")
    public String userList(@ModelAttribute com.finalProject.ali.admin.dto.UserSearchDTO searchDTO, Model model) {

        // 1. 데이터 조회
        List<UserDTO> users = userService.getUsersWithPaging(searchDTO);
        int totalCount = userService.getUsersCount(searchDTO);

        // 2. 페이징 계산 (전체 페이지 수)
        int totalPages = (int) Math.ceil((double) totalCount / searchDTO.getSize());

        // 3. 페이지 네비게이션 범위 계산 (예: 1 2 3 4 5)
        int startPage = Math.max(1, searchDTO.getPage() - 4);
        int endPage = Math.min(totalPages, startPage + 9);
        if (endPage == 0) endPage = 1; // 데이터가 없을 때 1페이지로 고정

        // 4. 모델 담기
        model.addAttribute("users", users);
        model.addAttribute("searchDTO", searchDTO); // 검색 상태 유지
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
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