package com.finalProject.ali.supplier_mypage.company.controller;

import com.finalProject.ali.supplier_mypage.common.controller.BaseSupplierController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SupplierCompanyController extends BaseSupplierController {

    @GetMapping("/supplier_mypage/company")
    public String company(Model model) {
        model.addAttribute("pageTitle", "회사 정보");
        model.addAttribute("activeMenu", "company");

        addCommonAttributes(model);

        return "supplier_mypage/company/index";
    }


}
