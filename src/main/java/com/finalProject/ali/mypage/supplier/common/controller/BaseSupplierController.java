package com.finalProject.ali.mypage.supplier.common.controller;

import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseSupplierController {

    protected void addCommonAttributes(Model model) {
        Map<String, Object> summary = defaultSummary();
        model.addAttribute("summary", summary);
    }

    protected void addCommonAttributes(Model model, Map<String, Object> summary) {
        model.addAttribute("summary", summary);
    }

    protected Map<String, Object> defaultSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("unreadNotifications", 0);
        summary.put("unreadMessages", 0);
        summary.put("inProgressOrders", 0);
        summary.put("openInquiries", 0);
        summary.put("shippingPending", 0);
        summary.put("settlementPending", 0);
        summary.put("returnRequested", 0);
        return summary;
    }
}


