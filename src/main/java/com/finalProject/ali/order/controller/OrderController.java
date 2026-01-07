package com.finalProject.ali.order.controller;

import com.finalProject.ali.order.dto.OrderCreateRequest;
import com.finalProject.ali.order.dto.OrderCreateResponse;
import com.finalProject.ali.order.service.OrderService;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public OrderCreateResponse create(@RequestBody OrderCreateRequest request, HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        String userId = user.getUserId();
        return orderService.createOrder(userId, request);
    }

}
