package com.finalProject.ali.supplier_mypage.common.controller;

import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.Map;


public abstract class BaseSupplierController {

    protected void addCommonAttributes(Model model) {

        // Topbar 알림/메시지 카운트
        Map<String, Object> summary = new HashMap<>();
        summary.put("unreadNotifications", 0);
        summary.put("unreadMessages", 0);

        // 대시보드에서도 공통으로 쓰는 값들(기본값)
        summary.put("inProgressOrders", 0);
        summary.put("openInquiries", 0);
        summary.put("shippingPending", 0);
        summary.put("settlementPending", 0);
        summary.put("returnRequested", 0);

        summary.put("latestInquiryAt", "-");
        summary.put("latestShippingPendingAt", "-");
        summary.put("latestReturnRequestedAt", "-");
        summary.put("latestSettlementPendingAt", "-");

        model.addAttribute("summary", summary);

        // 로그인 공급자 정보 (지금은 더미)
        Map<String, Object> supplier = new HashMap<>();
        supplier.put("companyName", "테스트 공급자");

        model.addAttribute("supplier", supplier);
    }
}
