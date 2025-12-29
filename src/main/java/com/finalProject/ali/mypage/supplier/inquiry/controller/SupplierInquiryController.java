package com.finalProject.ali.mypage.supplier.inquiry.controller;

import com.finalProject.ali.mypage.supplier.common.controller.BaseSupplierController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class SupplierInquiryController extends BaseSupplierController {

    @GetMapping("/mypage/supplier/inquiry")
    public String inquiry(Model model) {
        model.addAttribute("pageTitle", "문의 관리");
        model.addAttribute("activeMenu", "inquiry");

        addCommonAttributes(model);

        return "mypage/supplier/inquiry/index";
    }

}
