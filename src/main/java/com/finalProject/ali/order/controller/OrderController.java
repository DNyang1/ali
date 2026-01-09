package com.finalProject.ali.order.controller;

import com.finalProject.ali.order.dto.*;
import com.finalProject.ali.order.service.OrderService;
import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import com.finalProject.ali.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;
    @Autowired
    private UserService userService;

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

    @GetMapping("/sup")
    public List<SupplierOrderItemResponse> supOrders(HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        String userId = user.getUserId();
        SupplierDTO supplier = userService.getSupplierInfo(userId);
        String supplierId = supplier.getSupplierId();
        return orderService.getSupOrders(supplierId);
    }
}
