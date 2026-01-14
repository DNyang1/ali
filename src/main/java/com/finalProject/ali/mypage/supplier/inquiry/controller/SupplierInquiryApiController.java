package com.finalProject.ali.mypage.supplier.inquiry.controller;

import com.finalProject.ali.inquiry.dto.InquiryDTO;
import com.finalProject.ali.inquiry.service.InquiryService;
import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import com.finalProject.ali.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inquiry/supplier")
public class SupplierInquiryApiController {

    private final InquiryService inquiryService;
    private final UserService userService;

    @GetMapping("/recent")
    public List<InquiryDTO> recent(@RequestParam(defaultValue = "5") int limit,
                                   HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        String userId = user.getUserId();

        SupplierDTO supplier = userService.getSupplierInfo(userId);
        String supplierId = supplier.getSupplierId();

        return inquiryService.findRecentBySupplier(supplierId, limit);
    }


}
