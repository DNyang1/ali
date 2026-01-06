package com.finalProject.ali.cart.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cart")
public class CartViewController {

    @GetMapping
    public String cartPage(HttpSession session) {
        if (session.getAttribute("loginUser") == null) {
            return "redirect:user/login";
        }
        return "cart/cart";
    }
}