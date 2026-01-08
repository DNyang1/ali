package com.finalProject.ali.order.controller;

import com.finalProject.ali.order.dto.OrderCreateRequest;
import com.finalProject.ali.order.dto.OrderCreateResponse;
import com.finalProject.ali.order.dto.OrderDetailResponse;
import com.finalProject.ali.order.dto.OrderSummaryResponse;
import com.finalProject.ali.order.service.OrderService;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/my")
    public List<OrderSummaryResponse> myOrders(HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        return orderService.getMyOrders(user.getUserId());
    }

    @GetMapping("/{orderId}/detail")
    public OrderDetailResponse detail(
            @PathVariable Long orderId, HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        String userId = user.getUserId();
        return orderService.getOrderDetail(orderId, userId);
    }
}
