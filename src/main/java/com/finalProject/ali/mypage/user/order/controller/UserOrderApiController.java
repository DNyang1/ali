package com.finalProject.ali.mypage.user.order.controller;

import com.finalProject.ali.mypage.user.order.dto.UserRecentOrderResponse;
import com.finalProject.ali.mypage.user.order.service.UserOrderService;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage/user/order")
public class UserOrderApiController {

    private final UserOrderService userOrderService;

    @GetMapping("/recent")
    public List<UserRecentOrderResponse> recent(@RequestParam(defaultValue = "5") int limit,
                                                HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        if (user == null) return List.of();
        return userOrderService.findRecent(user.getUserId(), Math.min(limit, 20));
    }
}