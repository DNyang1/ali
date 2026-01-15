package com.finalProject.ali.mypage.user.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDashboardSummary {
    private int readyShippingCount;
    private int shippingCount;
    private int activeInquiryCount;
}
