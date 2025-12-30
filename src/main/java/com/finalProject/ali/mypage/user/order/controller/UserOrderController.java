package com.finalProject.ali.mypage.user.order.controller;

import com.finalProject.ali.mypage.user.common.controller.BaseUserController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserOrderController extends BaseUserController {

    @GetMapping("/mypage/user/order")
    public String order(Model model) {
        model.addAttribute("pageTitle", "주문 내역");
        model.addAttribute("activeMenu", "order");

        addCommonAttributes(model);

        return "mypage/user/order/index";
    }
}
