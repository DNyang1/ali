package com.finalProject.ali.order.controller;

import com.finalProject.ali.order.domain.OrderPreviewResponse;
import com.finalProject.ali.order.dto.DirectOrderRequest;
import com.finalProject.ali.order.dto.OrderCreateRequest;
import com.finalProject.ali.order.dto.OrderCreateResponse;
import com.finalProject.ali.order.service.OrderService;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public OrderCreateResponse create(@RequestBody OrderCreateRequest request, HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        String userId = user.getUserId();
        return orderService.createOrder(userId, request);
    }

    @GetMapping("/preview")
    public OrderPreviewResponse preview(HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        return orderService.getOrderPreview(user.getUserId());
    }
    @PostMapping("/preview/direct")
    public void previewDirect(@RequestBody DirectOrderRequest req, HttpSession session) {
        OrderPreviewResponse preview =
                orderService.getDirectOrderPreview(
                        req.getProductId(),
                        req.getSkuId(),
                        req.getQuantity()
                );
        session.setAttribute("directPreview", preview);
    }
    @GetMapping("/preview/current")
    public OrderPreviewResponse currentPreview(HttpSession session) {
         OrderPreviewResponse direct = (OrderPreviewResponse) session.getAttribute("directPreview");
        if (direct != null && direct.getItems() != null && !direct.getItems().isEmpty()) {
            session.removeAttribute("directPreview");
            return direct;
        }

        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        String userId = user.getUserId();
        return orderService.getOrderPreview(userId);
    }
}
