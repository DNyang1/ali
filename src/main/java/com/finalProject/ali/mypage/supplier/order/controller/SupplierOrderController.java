package com.finalProject.ali.mypage.supplier.order.controller;

import com.finalProject.ali.mypage.supplier.common.controller.BaseSupplierController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SupplierOrderController extends BaseSupplierController {

    @GetMapping("/mypage/supplier/order")
    public String order(Model model) {
        model.addAttribute("pageTitle", "주문 관리");
        model.addAttribute("activeMenu", "order");

        addCommonAttributes(model);

        return "mypage/supplier/order/index";
    }

    @GetMapping("/mypage/supplier/order/{orderItemId}")
    public String orderDetail(@PathVariable Long orderItemId, Model model) {
        model.addAttribute("orderItemId", orderItemId);

        addCommonAttributes(model);
        return "mypage/supplier/order/detail";
    }
}

