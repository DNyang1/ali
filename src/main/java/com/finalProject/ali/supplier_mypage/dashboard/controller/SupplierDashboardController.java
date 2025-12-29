package com.finalProject.ali.supplier_mypage.dashboard.controller;

import com.finalProject.ali.supplier_mypage.common.controller.BaseSupplierController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;

@Controller
public class SupplierDashboardController extends BaseSupplierController {

    @GetMapping("/supplier_mypage/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("pageTitle", "공급자 대시보드");
        model.addAttribute("activeMenu", "dashboard");

        // 공통 모델
        addCommonAttributes(model);

        // 대시보드 전용 리스트
        model.addAttribute("recentOrders", Collections.emptyList());
        model.addAttribute("recentInquiries", Collections.emptyList());
        model.addAttribute("notifications", Collections.emptyList());

        return "supplier_mypage/dashboard/index";
    }
}
