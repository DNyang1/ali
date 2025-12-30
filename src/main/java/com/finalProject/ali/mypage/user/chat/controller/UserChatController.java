package com.finalProject.ali.mypage.user.chat.controller;

import com.finalProject.ali.mypage.user.common.controller.BaseUserController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserChatController extends BaseUserController {

    @GetMapping("/mypage/user/chat")
    public String chat(Model model) {
        model.addAttribute("pageTitle", "채팅");
        model.addAttribute("activeMenu", "chat");

        addCommonAttributes(model);

        return "mypage/user/chat/index";
    }
}
