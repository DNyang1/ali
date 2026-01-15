package com.finalProject.ali.mypage.user.inquiry.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserRecentInquiryResponse {
    private Long inquiryId;
    private Long productId;
    private Long status;
    private LocalDateTime createdAt;
}
