package com.finalProject.ali.mypage.user.inquiry.controller;

import com.finalProject.ali.mypage.user.inquiry.dto.UserRecentInquiryResponse;
import com.finalProject.ali.mypage.user.inquiry.service.UserInquiryService;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inquiry/user")
public class UserInquiryApiController {

    private final UserInquiryService userInquiryService;

    @GetMapping("/recent")
    public List<UserRecentInquiryResponse> recent(@RequestParam(defaultValue = "5") int limit,
                                                  HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        if (user == null) return List.of();
        return userInquiryService.findRecent(user.getUserId(), Math.min(limit, 20));
    }
}
