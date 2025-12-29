package com.finalProject.ali.mypage.supplier.setting.controller;

import com.finalProject.ali.mypage.supplier.common.controller.BaseSupplierController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SupplierSettingController extends BaseSupplierController {

    @GetMapping("/mypage/supplier/setting")
    public String setting(Model model) {
        model.addAttribute("pageTitle", "설정");
        model.addAttribute("activeMenu", "setting");

        addCommonAttributes(model);

        return "/mypage/supplier/setting/index";
    }

}
