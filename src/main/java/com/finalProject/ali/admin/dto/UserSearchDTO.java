package com.finalProject.ali.admin.dto;

import lombok.Data;

@Data
public class UserSearchDTO {
    private String keyword;   // 검색어 (ID, 이름)
    private String role;      // 권한 (ROLE_USER, ROLE_SUPPLIER, ROLE_ADMIN)
    private String status;    // 상태 (ACTIVE, SUSPENDED)

    private int page = 1;     // 현재 페이지 (기본 1)
    private int size = 10;    // 페이지당 개수 (기본 10)

    // MyBatis에서 LIMIT 시작점 계산용 (getter)
    public int getOffset() {
        return (page - 1) * size;
    }
}