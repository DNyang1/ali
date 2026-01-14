package com.finalProject.ali.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardDTO {
    private int totalUsers;        // 총 회원 수
    private int todayUsers;        // 오늘 가입자 수
    private int pendingSuppliers;  // 승인 대기 중인 판매자 수
    private long waitingQna;       // 답변 대기 중인 문의 수

}