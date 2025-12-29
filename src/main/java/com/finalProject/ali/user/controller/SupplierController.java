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

    @GetMapping("/index")
    public String supplierIndex(HttpSession session, Model model) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/user/login";

        SupplierDTO supplier = userService.getSupplierInfo(loginUser.getUserId());
        model.addAttribute("supplier", supplier);
        return "supplier/index";
    }

    @GetMapping("/update")
    public String updatePage(HttpSession session, Model model) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/user/login";

        SupplierDTO supplier = userService.getSupplierInfo(loginUser.getUserId());
        model.addAttribute("supplier", supplier);
        return "supplier/update";
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<String> updateSupplier(@RequestBody SupplierDTO supplierDTO, HttpSession session) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser != null) {
            supplierDTO.setUserId(loginUser.getUserId());
            userService.updateSupplier(supplierDTO);
            return ResponseEntity.ok("success");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("fail");
    }
}