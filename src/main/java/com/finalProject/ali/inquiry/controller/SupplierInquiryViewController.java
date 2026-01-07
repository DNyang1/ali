package com.finalProject.ali.inquiry.controller;

import com.finalProject.ali.inquiry.dao.SupplierLookupDAO;
import com.finalProject.ali.inquiry.dto.InquiryDTO;
import com.finalProject.ali.inquiry.service.InquiryService;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/inquiry/supplier")
@Slf4j
public class SupplierInquiryViewController {

    private final InquiryService inquiryService;
    private final SupplierLookupDAO supplierLookupDAO;

    private String getLoginId(HttpSession session) {
        Object v = session.getAttribute("loginUser");
        if (v == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        if (v instanceof UserDTO user) {
            String userId = user.getUserId();
            if (userId == null || userId.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보가 올바르지 않습니다.");
            }
            return userId;
        }

        // 혹시 문자열로 저장되는 경우
        String loginId = v.toString();
        if (loginId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보가 올바르지 않습니다.");
        }
        return loginId;
    }

    private String getSupplierId(HttpSession session) {

        String loginId = getLoginId(session);
        log.info("loginId(session)={}", loginId);

        // 이미 supplier_id 형태면 그대로 사용
        if (loginId.startsWith("s_")) {
            return loginId;
        }

        // user_id → supplier_id 조회 (APPROVED만)
        String supplierId = supplierLookupDAO.findApprovedSupplierIdByUserId(loginId);
        log.info("supplierId(from db)={}", supplierId);

        if (supplierId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "판매자 승인(APPROVED)이 필요합니다.");
        }

        return supplierId;
    }

    // 판매자 문의 목록 (기본 상태 = 0)
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") Long status, HttpSession session, Model model) {

        String supplierId = getSupplierId(session);

        List<InquiryDTO> inquiries = inquiryService.findBySupplier(supplierId, status);
        model.addAttribute("inquiries", inquiries);
        model.addAttribute("status", status);

        return "inquiry/supplier/list";
    }

    // 판매자 문의 상세 (해당 판매자 문의만)
    @GetMapping("/detail/{inquiryId}")
    public String detail(@PathVariable Long inquiryId, HttpSession session, Model model) {

        String supplierId = getSupplierId(session);

        InquiryDTO inquiry = inquiryService.findById(inquiryId);
        if (inquiry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "문의가 없습니다.");
        }

        if (!supplierId.equals(inquiry.getSupplierId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "조회 권한이 없습니다.");
        }

        model.addAttribute("inquiry", inquiry);
        return "inquiry/supplier/detail";
    }
}
