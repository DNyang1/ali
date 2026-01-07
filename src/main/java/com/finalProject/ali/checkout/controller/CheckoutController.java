package com.finalProject.ali.checkout.controller;

import com.finalProject.ali.checkout.dto.CheckoutRequest;
import com.finalProject.ali.checkout.dto.CheckoutResponse;
import com.finalProject.ali.checkout.service.CheckoutService;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping
    public CheckoutResponse checkout(
            @RequestBody CheckoutRequest request, HttpSession session){
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        String userId = user.getUserId();

        return checkoutService.checkout(userId, request);
    }
}
