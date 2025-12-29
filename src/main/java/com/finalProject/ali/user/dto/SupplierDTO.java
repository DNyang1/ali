package com.finalProject.ali.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierDTO {
    private String supplierId;
    private String userId;
    private String cpNumber;
    private String cpName;
    private String cpAddress;
    private String cpPhone;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
