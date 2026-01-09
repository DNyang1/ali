package com.finalProject.ali.mypage.user.inquiry.controller;

import com.finalProject.ali.inquiry.dto.InquiryDTO;
import com.finalProject.ali.inquiry.service.InquiryService;
import com.finalProject.ali.inquiry.service.InquiryWorkflowService;
import com.finalProject.ali.product.dao.CustomOrderSheetDAO;
import com.finalProject.ali.product.sheet.status.SheetStatus;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/inquiry/user")
public class UserInquiryActionController {

    private final InquiryService inquiryService;
    private final CustomOrderSheetDAO customOrderSheetDAO;
    private final InquiryWorkflowService inquiryWorkflowService;

    private String getLoginId(HttpSession session) {
        Object v = session.getAttribute("loginUser");
        if (v == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");

        if (v instanceof UserDTO user) {
            String userId = user.getUserId();
            if (userId == null || userId.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보가 올바르지 않습니다.");
            }
            return userId;
        }

        String loginId = v.toString();
        if (loginId.isBlank()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보가 올바르지 않습니다.");
        return loginId;
    }

    @PostMapping("/detail/{inquiryId}/cancel")
    public String cancel(@PathVariable Long inquiryId, HttpSession session, RedirectAttributes ra) {
        try {
            String userId = getLoginId(session);
            inquiryWorkflowService.userCancelInquiry(inquiryId, userId);
            ra.addFlashAttribute("msg", "문의가 취소되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inquiry/user/detail/" + inquiryId;
    }


    @PostMapping("/detail/{inquiryId}/reject")
    public String rejectQuote(@PathVariable Long inquiryId,
                              HttpSession session,
                              RedirectAttributes ra) {
        try {
            String userId = getLoginId(session);
            inquiryWorkflowService.userRejectQuote(inquiryId, userId); // 서비스로 이동
            ra.addFlashAttribute("msg", "견적을 거절했습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inquiry/user/detail/" + inquiryId;
    }

    @PostMapping("/inquiry/user/{inquiryId}/pay")
    public String pay(@PathVariable Long inquiryId) {
        // TODO: 결제 처리 로직
        return "redirect:/inquiry/user/detail/" + inquiryId;
    }

}
