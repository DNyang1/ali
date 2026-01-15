package com.finalProject.ali.mypage.user.dashboard.controller;

import com.finalProject.ali.mypage.user.common.controller.BaseUserController;
import com.finalProject.ali.mypage.user.dashboard.dto.Summary;
import com.finalProject.ali.mypage.user.dashboard.dto.UserDashboardSummary;
import com.finalProject.ali.mypage.user.dashboard.dto.UserProfileView;
import com.finalProject.ali.mypage.user.dashboard.service.UserDashboardService;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserDashboardController extends BaseUserController {

    private final UserDashboardService dashboardService;

    @GetMapping("/mypage/user/dashboard")
    public String userDashboard(HttpSession session, Model model) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        String userId = loginUser.getUserId();


        UserDashboardSummary s = dashboardService.getSummary(userId);
        model.addAttribute("summary", s);


        model.addAttribute("recentOrders", List.of());
        model.addAttribute("favoriteProducts", List.of());
        model.addAttribute("recentInquiries", List.of());

        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("user", new UserProfileView(
                loginUser.getName(),
                loginUser.getEmail()
        ));

        return "mypage/user/dashboard/index";
    }
}
