package com.finalProject.ali.user.controller;

import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // 👈 중요: 시큐리티 인증 정보 사용

@Controller
@RequestMapping("/supplier")
public class SupplierController {

    @Autowired
    private UserService userService;

    // 1. 판매자 설정 페이지
    @GetMapping("/s_setting")
    public String supplierSetting(Principal principal, Model model) {

        SupplierDTO supplier = userService.getSupplierInfo(principal.getName());
        model.addAttribute("supplier", supplier);
        return "supplier/s_setting";
    }

    // 2. 정보 수정 페이지
    @GetMapping("/s_update")
    public String updatePage(Principal principal, Model model) {
        SupplierDTO supplier = userService.getSupplierInfo(principal.getName());
        model.addAttribute("supplier", supplier);
        return "supplier/s_update";
    }

    // 3. 정보 수정 처리 (AJAX)
    @PostMapping("/s_update")
    @ResponseBody
    public ResponseEntity<String> updateSupplier(@RequestBody SupplierDTO supplierDTO, Principal principal) {
        // principal이 null일 가능성은 없음 (Security 설정 덕분)

        supplierDTO.setUserId(principal.getName()); // 현재 로그인한 ID 주입
        userService.updateSupplier(supplierDTO);

        return ResponseEntity.ok("success");
    }
}