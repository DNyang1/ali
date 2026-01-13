package com.finalProject.ali.mypage.supplier.order.controller;

import com.finalProject.ali.mypage.supplier.common.controller.BaseSupplierController;
import com.finalProject.ali.mypage.supplier.order.service.SupplierShippingService;
import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import com.finalProject.ali.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@RequiredArgsConstructor
@Controller
public class SupplierOrderController extends BaseSupplierController {
    private final UserService userService;
    private final SupplierShippingService shippingService;

//    @GetMapping("/mypage/supplier/order")
//    public String order(Model model) {
//        model.addAttribute("pageTitle", "주문 관리");
//        model.addAttribute("activeMenu", "order");
//
//        addCommonAttributes(model);
//
//        return "mypage/supplier/order/index";
//    }
    @GetMapping("/mypage/supplier/order")
    public String orderList(@RequestParam(required = false) String status,
                            HttpSession session,
                            Model model) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/user/login";

        SupplierDTO supplier = userService.getSupplierInfo(loginUser.getUserId());
        String supplierId = supplier.getSupplierId();

        model.addAttribute("pageTitle", "주문/배송 관리");
        model.addAttribute("activeMenu", "order");

        if ("READY".equals(status)) {
            model.addAttribute("items", shippingService.listReadyItems(supplierId));
        } else {
            model.addAttribute("items", List.of()); // 너희 기존 주문 목록 넣는 자리
        }

        model.addAttribute("status", status);
        return "mypage/supplier/order/index"; // 너 템플릿 경로 맞춰
    }

    @PostMapping("/mypage/supplier/order-item/{orderItemId}/ship")
    public String ship(@PathVariable Long orderItemId,
                       @RequestParam String carrier,
                       @RequestParam String trackingNo) {

        shippingService.ship(orderItemId, carrier, trackingNo);
        return "redirect:/mypage/supplier/order?status=READY";
    }
    @GetMapping("/mypage/supplier/order/{orderItemId}")
    public String orderDetail(@PathVariable Long orderItemId, Model model) {
        model.addAttribute("orderItemId", orderItemId);

        addCommonAttributes(model);
        return "mypage/supplier/order/detail";
    }
}

