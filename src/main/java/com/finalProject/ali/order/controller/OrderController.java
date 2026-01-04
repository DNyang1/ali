package com.finalProject.ali.order.controller;

import com.finalProject.ali.order.domain.OrderPreviewResponse;
import com.finalProject.ali.order.dto.OrderCreateResponse;
import com.finalProject.ali.order.service.OrderService;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public OrderCreateResponse create(HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        String userId = user.getUserId();
        return orderService.createOrder(userId);
    }

    @GetMapping("/orders/preview")
    public OrderPreviewResponse preview(HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        return orderService.getOrderPreview(user.getUserId());
    }
}
