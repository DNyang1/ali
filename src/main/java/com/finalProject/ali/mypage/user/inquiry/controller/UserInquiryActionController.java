package com.finalProject.ali.mypage.user.inquiry.controller;

import com.finalProject.ali.inquiry.service.InquiryWorkflowService;
import com.finalProject.ali.product.dao.CustomOrderSheetDAO;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/inquiry/user")
public class UserInquiryActionController {

    private final InquiryWorkflowService inquiryWorkflowService;
    private final CustomOrderSheetDAO customOrderSheetDAO;

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

    @PostMapping("/{inquiryId}/pay-start")
    @ResponseBody
    public void payStart(@PathVariable Long inquiryId,
                         @RequestBody Map<String, Long> body,
                         HttpSession session) {

        Long sheetId = body.get("sheetId");
        if (sheetId == null) throw new IllegalArgumentException("sheetId 없음");

        var sheet = customOrderSheetDAO.findPayableBySheetAndInquiry(sheetId, inquiryId);
        if (sheet == null) throw new IllegalStateException("결제 가능한 주문서가 아닙니다.");

        session.setAttribute("checkoutSheetId", sheetId);
    }


}
