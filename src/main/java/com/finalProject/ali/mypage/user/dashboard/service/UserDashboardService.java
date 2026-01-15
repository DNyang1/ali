package com.finalProject.ali.mypage.user.dashboard.service;

import com.finalProject.ali.mypage.user.dashboard.dao.UserDashboardDAO;
import com.finalProject.ali.mypage.user.dashboard.dto.UserDashboardSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDashboardService {

    private final UserDashboardDAO dashboardDAO;

    public UserDashboardSummary getSummary(String userId) {
        UserDashboardSummary s = new UserDashboardSummary(
                dashboardDAO.countReadyShipping(userId),
                dashboardDAO.countShipping(userId),
                dashboardDAO.countActiveInquiry(userId)
        );
        return s;
    }

}
