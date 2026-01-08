package com.finalProject.ali.inquiry.controller;

import com.finalProject.ali.inquiry.dto.InquiryCreateRequest;
import com.finalProject.ali.inquiry.dto.InquiryDTO;
import com.finalProject.ali.inquiry.service.InquiryService;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @GetMapping("/create")
    public String writeForm(@RequestParam Long productId, Model model) {
        InquiryCreateRequest req = new InquiryCreateRequest();
        req.setProductId(productId);
        model.addAttribute("req", req);
        return "inquiry/create";
    }


    @PostMapping
    public String createInquiry(InquiryCreateRequest request,
                                @RequestParam(required=false) Long productId,
                                HttpSession session) {

        System.out.println(">>> request.productId = " + request.getProductId());
        System.out.println(">>> @RequestParam productId = " + productId);

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        // request로 안 들어오면 @RequestParam 값으로 강제 주입해서라도 진행
        if (request.getProductId() == null) {
            request.setProductId(productId);
        }

        inquiryService.createInquiry(loginUser.getUserId(), request);
        return "redirect:/inquiry/user/list";
    }


}
