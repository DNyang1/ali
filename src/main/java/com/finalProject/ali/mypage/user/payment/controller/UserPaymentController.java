package com.finalProject.ali.mypage.user.payment.controller;

import com.finalProject.ali.mypage.user.common.controller.BaseUserController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserPaymentController extends BaseUserController {

    @GetMapping("/mypage/user/payment")
    public String payment(Model model) {
        model.addAttribute("pageTitle", "결제 내역");
        model.addAttribute("activeMenu", "payment");

        addCommonAttributes(model);

        return "mypage/user/payment/index";
    }
}
