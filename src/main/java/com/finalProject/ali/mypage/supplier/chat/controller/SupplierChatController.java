package com.finalProject.ali.mypage.supplier.chat.controller;

import com.finalProject.ali.mypage.supplier.common.controller.BaseSupplierController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class SupplierChatController extends BaseSupplierController {

    @GetMapping("/mypage/supplier/chat")
    public String chat(Model model) {
        model.addAttribute("pageTitle", "채팅 / 메시지");
        model.addAttribute("activeMenu", "chat");

        addCommonAttributes(model);

        return "mypage/supplier/chat/index";
    }

}
