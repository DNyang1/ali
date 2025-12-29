package com.finalProject.ali.user.controller;

import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import com.finalProject.ali.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/supplier")
public class SupplierController {

    @Autowired
    private UserService userService;

    // 1. 공급자 메인 페이지 (공급자 정보 조회 포함)
    @GetMapping("/index")
    public String supplierIndex(HttpSession session, Model model) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/user/login";

        // DB에서 현재 로그인한 유저의 공급자 정보를 가져옴
        SupplierDTO supplier = userService.getSupplierInfo(loginUser.getUserId());
        // 화면(HTML)으로 정보를 전달
        model.addAttribute("supplier", supplier);
        return "supplier/index";
    }

    // 2. 공급자 정보 수정 페이지 이동
    @GetMapping("/update")
    public String updatePage(HttpSession session, Model model) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/user/login";

        SupplierDTO supplier = userService.getSupplierInfo(loginUser.getUserId());
        model.addAttribute("supplier", supplier);
        return "supplier/update";
    }

    // 3. 공급자 정보 수정 처리 (비동기 요청)
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<String> updateSupplier(@RequestBody SupplierDTO supplierDTO, HttpSession session) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser != null) {
            // 보안을 위해 세션의 아이디를 강제 세팅
            supplierDTO.setUserId(loginUser.getUserId());
            userService.updateSupplier(supplierDTO);
            return ResponseEntity.ok("success");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("fail");
    }
}