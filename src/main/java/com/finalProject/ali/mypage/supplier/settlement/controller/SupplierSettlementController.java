package com.finalProject.ali.mypage.supplier.settlement.controller;

import com.finalProject.ali.mypage.supplier.common.controller.BaseSupplierController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class SupplierSettlementController extends BaseSupplierController {

    @GetMapping("/mypage/supplier/settlement")
    public String settlement(Model model) {
        model.addAttribute("pageTitle", "정산");
        model.addAttribute("activeMenu", "settlement");

        addCommonAttributes(model);

        return "/mypage/supplier/settlement/index";
    }

}
