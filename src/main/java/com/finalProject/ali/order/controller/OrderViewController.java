package com.finalProject.ali.order.controller;

import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/order")
public class OrderViewController {

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }
        return "checkout/checkout";
    }

    @GetMapping("/complete")
    public String complete(@RequestParam Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "order/complete";
    }
}
