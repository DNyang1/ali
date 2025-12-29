package com.finalProject.ali.supplier_mypage.inquiry.controller;

import com.finalProject.ali.supplier_mypage.common.controller.BaseSupplierController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class SupplierInquiryController extends BaseSupplierController {

    @GetMapping("/supplier_mypage/inquiry")
    public String inquiry(Model model) {
        model.addAttribute("pageTitle", "문의 관리");
        model.addAttribute("activeMenu", "inquiry");

        addCommonAttributes(model);

        return "supplier_mypage/inquiry/index";
    }

}
