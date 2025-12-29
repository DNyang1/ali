package com.finalProject.ali.supplier_mypage.setting.controller;

import com.finalProject.ali.supplier_mypage.common.controller.BaseSupplierController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SupplierSettingController extends BaseSupplierController {

    @GetMapping("/supplier_mypage/setting")
    public String setting(Model model) {
        model.addAttribute("pageTitle", "설정");
        model.addAttribute("activeMenu", "setting");

        addCommonAttributes(model);

        return "supplier_mypage/setting/index";
    }

}
