package com.finalProject.ali.inquiry.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InquiryStatusUpdateRequest {
    // 0: 처리 중
    // 1: 완료
    // 2: 반려
    private Long status;
}
