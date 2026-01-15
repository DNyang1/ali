package com.finalProject.ali.mypage.user.order.service;

import com.finalProject.ali.mypage.user.order.dao.UserOrderDAO;
import com.finalProject.ali.mypage.user.order.dto.UserRecentOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserOrderService {

    private final UserOrderDAO userOrderDAO;

    public List<UserRecentOrderResponse> findRecent(String userId, int limit) {
        return userOrderDAO.findRecentOrdersByUserId(userId, limit);
    }
}

