package com.finalProject.ali.supplier_mypage.order.controller;

import com.finalProject.ali.supplier_mypage.common.controller.BaseSupplierController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SupplierOrderController extends BaseSupplierController {

    @GetMapping("/supplier_mypage/order")
    public String order(Model model) {
        model.addAttribute("pageTitle", "주문 관리");
        model.addAttribute("activeMenu", "order");

        addCommonAttributes(model);

        return "supplier_mypage/order/index";
    }


}
