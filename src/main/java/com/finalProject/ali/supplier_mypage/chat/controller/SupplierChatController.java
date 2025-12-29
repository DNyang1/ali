package com.finalProject.ali.supplier_mypage.chat.controller;

import com.finalProject.ali.supplier_mypage.common.controller.BaseSupplierController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class SupplierChatController extends BaseSupplierController {

    @GetMapping("/supplier_mypage/chat")
    public String chat(Model model) {
        model.addAttribute("pageTitle", "채팅 / 메시지");
        model.addAttribute("activeMenu", "chat");

        addCommonAttributes(model);

        return "supplier_mypage/chat/index";
    }

}
