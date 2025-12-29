package com.finalProject.ali.supplier_mypage.settlement.controller;

import com.finalProject.ali.supplier_mypage.common.controller.BaseSupplierController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class SupplierSettlementController extends BaseSupplierController {

    @GetMapping("/supplier_mypage/settlement")
    public String settlement(Model model) {
        model.addAttribute("pageTitle", "정산");
        model.addAttribute("activeMenu", "settlement");

        addCommonAttributes(model);

        return "supplier_mypage/settlement/index";
    }

}
