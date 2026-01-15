package com.finalProject.ali.mypage.user.inquiry.service;

import com.finalProject.ali.mypage.user.inquiry.dao.UserInquiryDAO;
import com.finalProject.ali.mypage.user.inquiry.dto.UserRecentInquiryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserInquiryService {

    private final UserInquiryDAO userInquiryDAO;

    public List<UserRecentInquiryResponse> findRecent(String userId, int limit) {
        return userInquiryDAO.findRecentByUserId(userId, limit);
    }
}
