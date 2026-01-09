package com.finalProject.ali.inquiry.controller;

import com.finalProject.ali.inquiry.dto.InquiryDTO;
import com.finalProject.ali.inquiry.service.InquiryService;
import com.finalProject.ali.product.dao.CustomOrderSheetDAO;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/inquiry/user")
public class UserInquiryViewController {

    private final InquiryService inquiryService;
    private final CustomOrderSheetDAO customOrderSheetDAO;


    private String getLoginId(HttpSession session) {
        Object v = session.getAttribute("loginUser");
        if (v == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        // loginUser가 UserDTO로 저장되는 경우
        if (v instanceof UserDTO user) {
            String userId = user.getUserId();
            if (userId == null || userId.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보가 올바르지 않습니다.");
            }
            return userId;
        }

        // 혹시 String으로 저장된 경우
        return v.toString();
    }

    // 구매자 문의 목록
    @GetMapping("/list")
    public String list(HttpSession session, Model model) {
        String userId = getLoginId(session);

        List<InquiryDTO> inquiries = inquiryService.findMyInquiries(userId);
        model.addAttribute("inquiries", inquiries);

        return "inquiry/user/list";
    }

    // 구매자 문의 상세 (본인만)
    @GetMapping("/detail/{inquiryId}")
    public String detail(@PathVariable Long inquiryId,
                         HttpSession session,
                         Model model) {

        String userId = getLoginId(session);

        InquiryDTO inquiry = inquiryService.findById(inquiryId);
        if (inquiry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "문의가 없습니다.");
        }

        if (!userId.equals(inquiry.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "조회 권한이 없습니다.");
        }

        model.addAttribute("inquiry", inquiry);
        model.addAttribute("isUserView", true);

        model.addAttribute("sheet", customOrderSheetDAO.findByInquiryId(inquiryId));
        return "inquiry/user/detail";
    }
}
