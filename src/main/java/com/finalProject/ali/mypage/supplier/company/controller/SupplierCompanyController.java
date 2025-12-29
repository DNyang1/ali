package com.finalProject.ali.mypage.supplier.company.controller;

import com.finalProject.ali.mypage.supplier.common.controller.BaseSupplierController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SupplierCompanyController extends BaseSupplierController {

    @GetMapping("/mypage/supplier/company")
    public String company(Model model) {
        model.addAttribute("pageTitle", "회사 정보");
        model.addAttribute("activeMenu", "company");

        addCommonAttributes(model);

        return "mypage/supplier/company/index";
    }


}
