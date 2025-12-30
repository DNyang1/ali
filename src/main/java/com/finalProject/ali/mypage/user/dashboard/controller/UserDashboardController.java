package com.finalProject.ali.mypage.user.dashboard.controller;

import com.finalProject.ali.mypage.user.common.controller.BaseUserController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserDashboardController extends BaseUserController {

    @GetMapping("/mypage/user/dashboard")
    public String userDashboard(Model model){
        model.addAttribute("pageTitle", "유저 홈");
        model.addAttribute("activeMenu", "dashboard");
        return "mypage/user/dashboard/index";
    }
}
