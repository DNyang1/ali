package com.finalProject.ali.mypage.supplier.dashboard.controller;

import com.finalProject.ali.mypage.supplier.common.controller.BaseSupplierController;
import com.finalProject.ali.mypage.supplier.dashboard.service.SupplierDashboardService;
import com.finalProject.ali.product.service.ProductService;
import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import com.finalProject.ali.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SupplierDashboardController extends BaseSupplierController {

    private final UserService userService;
    private final SupplierDashboardService dashboardService;
    private final ProductService productService;

    @GetMapping("/mypage/supplier/dashboard")
    public String dashboard(HttpSession session, Model model) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/user/login";

        model.addAttribute("pageTitle", "공급자 대시보드");
        model.addAttribute("activeMenu", "dashboard");

        SupplierDTO supplier = userService.getSupplierInfo(loginUser.getUserId());
        model.addAttribute("supplier", supplier);

        String supplierId = supplier.getSupplierId();   // ← 여기서 supplierId 확보

        Map<String, Object> summary = defaultSummary();
        summary.put("openInquiries", dashboardService.countOpenInquiries(supplierId));
        summary.put("inProgressInquiries", dashboardService.countInProgressInquiries(supplierId));
        summary.put("lowStockProductCount",
                productService.countLowStockProducts(supplierId, 5));
        addCommonAttributes(model, summary);


        model.addAttribute("recentOrders", Collections.emptyList());
        model.addAttribute("recentInquiries", Collections.emptyList());
        model.addAttribute("notifications", Collections.emptyList());



        return "mypage/supplier/dashboard/index";
    }
}
