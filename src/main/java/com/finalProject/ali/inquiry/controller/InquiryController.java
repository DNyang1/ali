package com.finalProject.ali.inquiry.controller;

import com.finalProject.ali.inquiry.dto.InquiryCreateRequest;
import com.finalProject.ali.inquiry.dto.InquiryDTO;
import com.finalProject.ali.inquiry.service.InquiryService;
import com.finalProject.ali.product.dao.ProductDAO;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;
    private final ProductDAO productDAO;

    private String normalizeUserId(String id) {
        if (id == null) return null;
        return id.startsWith("s_") ? id.substring(2) : id;
    }

    @GetMapping("/create")
    public String writeForm(@RequestParam Long productId, Model model, HttpSession session) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        String supplierId = productDAO.findSupplierIdByProductId(productId);
        if (supplierId == null) return "redirect:/products/" + productId;

        String u = normalizeUserId(loginUser.getUserId());
        String s = normalizeUserId(supplierId);

        if (u != null && u.equals(s)) {
            return "redirect:/products/" + productId + "?error=self";
        }

        InquiryCreateRequest req = new InquiryCreateRequest();
        req.setProductId(productId);
        model.addAttribute("req", req);
        return "inquiry/create";
    }

    @PostMapping
    public String createInquiry(InquiryCreateRequest request, @RequestParam(required=false) Long productId, HttpSession session, RedirectAttributes ra) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        if (request.getProductId() == null) {
            request.setProductId(productId);
        }

        try {
            inquiryService.createInquiry(loginUser.getUserId(), request);
            return "redirect:/inquiry/user/list";
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/products/" + request.getProductId();
        }
    }
}
