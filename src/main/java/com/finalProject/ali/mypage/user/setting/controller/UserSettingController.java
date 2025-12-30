package com.finalProject.ali.mypage.user.setting.controller;

import com.finalProject.ali.mypage.user.common.controller.BaseUserController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserSettingController extends BaseUserController {

    @GetMapping("/mypage/user/setting")
    public String setting(Model model) {
        model.addAttribute("pageTitle", "계정 설정");
        model.addAttribute("activeMenu", "setting");

        addCommonAttributes(model);

        return "mypage/user/setting/index";
    }
}
