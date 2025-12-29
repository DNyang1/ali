package com.finalProject.ali.mypage.supplier.dashboard.controller;

import com.finalProject.ali.mypage.supplier.common.controller.BaseSupplierController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;

@Controller
public class SupplierDashboardController extends BaseSupplierController {

    @GetMapping("/mypage/supplier/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("pageTitle", "공급자 대시보드");
        model.addAttribute("activeMenu", "dashboard");

        addCommonAttributes(model);

        model.addAttribute("recentOrders", Collections.emptyList());
        model.addAttribute("recentInquiries", Collections.emptyList());
        model.addAttribute("notifications", Collections.emptyList());

        return "mypage/supplier/dashboard/index";
    }
}
