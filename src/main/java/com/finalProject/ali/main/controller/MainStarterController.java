package com.finalProject.ali.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainStarterController {

    @GetMapping("/starter")
    public String starter(Model model) {
        model.addAttribute("pageTitle", "메인 스타터 템플릿");
        return "main/starter";
    }
}