package com.finalProject.ali.mypage.user.dashboard.controller;

import com.finalProject.ali.mypage.user.common.controller.BaseUserController;
import com.finalProject.ali.mypage.user.dashboard.dto.Summary;
import com.finalProject.ali.mypage.user.dashboard.dto.UserProfileView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UserDashboardController extends BaseUserController {

    @GetMapping("/mypage/user/dashboard")
    public String userDashboard(Model model){
        model.addAttribute("summary", new Summary(0,0,0,0));
        model.addAttribute("recentOrders", List.of());
        model.addAttribute("favoriteProducts", List.of());
        model.addAttribute("recentInquiries", List.of());
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("user", new UserProfileView("사용자", "user@email.com"));
        return "mypage/user/dashboard/index";
    }
}
