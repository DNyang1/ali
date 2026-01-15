package com.finalProject.ali.user.controller;

import com.finalProject.ali.image.service.ImageService;
import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import com.finalProject.ali.user.dto.UserUpdateDTO;
import com.finalProject.ali.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserAccountController {

    @Autowired private UserService userService;
    @Autowired private ImageService imageService;

    // 마이페이지
    @GetMapping("/setting")
    public String setting(Model model, Principal principal) {
        if (principal == null) return "redirect:/user/login";
        UserDTO loginUser = userService.findByUserId(principal.getName());
        model.addAttribute("user", loginUser);
        return "user/setting";
    }

    // 정보 수정 페이지
    @GetMapping("/update")
    public String updatePage(HttpSession session) {
        if (session.getAttribute("loginUser") == null) return "redirect:/user/login";
        return "user/update";
    }

    // [정보 수정] DTO 변경 적용
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<String> update(
            @RequestPart("userData") UserUpdateDTO updateRequest, // 변경된 DTO 사용
            @RequestPart(value = "profileFile", required = false) MultipartFile profileFile,
            HttpSession session,
            Principal principal) {

        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("fail");
        String currentUserId = principal.getName();
        UserDTO loginUser = userService.findByUserId(currentUserId);

        // 변환 전 DTO에 프로필 경로 세팅
        if (profileFile != null && !profileFile.isEmpty()) {
            if (loginUser.getProfileImg() != null) {
                imageService.deleteActualFile(loginUser.getProfileImg());
            }
            String uploadedPath = imageService.uploadImage(profileFile, "profiles");
            updateRequest.setProfileImg(uploadedPath);
        } else {
            updateRequest.setProfileImg(loginUser.getProfileImg());
        }

        // DTO 변환 후 업데이트
        UserDTO userDTO = updateRequest.toUserDTO(currentUserId);
        userService.updateUserInfo(userDTO);

        // 세션 갱신
        UserDTO updatedUser = userService.findByUserId(currentUserId);
        session.setAttribute("loginUser", updatedUser);

        return ResponseEntity.ok("success");
    }

    @PostMapping("/delete_profile_img")
    @ResponseBody
    public ResponseEntity<String> deleteProfileImg(HttpSession session, Principal principal) {
        try {
            if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("fail");
            UserDTO loginUser = userService.findByUserId(principal.getName());

            if (loginUser.getProfileImg() != null) {
                imageService.deleteActualFile(loginUser.getProfileImg());
            }
            loginUser.setProfileImg(null);
            userService.updateUserInfo(loginUser);
            session.setAttribute("loginUser", loginUser);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    // 비밀번호 변경
    @GetMapping("/update_password")
    public String updatePasswordPage() { return "user/update_password"; }

    @PostMapping("/update_password")
    @ResponseBody
    public ResponseEntity<String> updatePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            Principal principal, HttpSession session) {
        boolean isChanged = userService.changePassword(principal.getName(), currentPassword, newPassword);
        if (isChanged) {
            session.invalidate();
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("wrong_password");
        }
    }

    // 판매자 전환 관련
    @GetMapping("/switch-role")
    public String switchRole(
            @RequestParam(value = "reapply", required = false) Boolean reapply,
            HttpSession session, Model model, Principal principal) {
        if (principal == null) return "redirect:/user/login";
        SupplierDTO supplier = userService.getSupplierInfo(principal.getName());
        if (supplier == null) return "user/supplier_register";

        String status = supplier.getStatus();
        if ("APPROVED".equals(status)) {
            session.setAttribute("supplierInfo", supplier);
            return "redirect:/mypage/supplier/dashboard";
        } else if ("REJECTED".equals(status) && Boolean.TRUE.equals(reapply)) {
            model.addAttribute("supplier", supplier);
            return "user/supplier_register";
        } else {
            model.addAttribute("status", status);
            model.addAttribute("supplier", supplier);
            return "user/supplier_status";
        }
    }

    @PostMapping("/supplier-signup")
    @ResponseBody
    public ResponseEntity<String> supplierSignup(@RequestBody SupplierDTO supplierDTO, Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("fail");
        supplierDTO.setUserId(principal.getName());
        supplierDTO.setSupplierId("s_" + principal.getName());
        userService.registerSupplier(supplierDTO);
        return ResponseEntity.ok("success");
    }
}