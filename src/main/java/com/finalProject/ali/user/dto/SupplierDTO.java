package com.finalProject.ali.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierDTO {
    private String supplierId;
    private String userId;    // users 테이블의 PK와 매핑
    private String cpNumber;  // 사업자 번호
    private String cpName;    // 상호명
    private String cpAddress; // 사업장 주소
    private String cpPhone;   // 사업장 전화번호
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
