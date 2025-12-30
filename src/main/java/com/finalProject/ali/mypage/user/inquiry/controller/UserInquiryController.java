package com.finalProject.ali.mypage.user.inquiry.controller;

import com.finalProject.ali.mypage.user.common.controller.BaseUserController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserInquiryController extends BaseUserController {

    @GetMapping("/mypage/user/inquiry")
    public String inquiry(Model model) {
        model.addAttribute("pageTitle", "문의 내역");
        model.addAttribute("activeMenu", "inquiry");

        addCommonAttributes(model);

        return "mypage/user/inquiry/index";
    }
}
