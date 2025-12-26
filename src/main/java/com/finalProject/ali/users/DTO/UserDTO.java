package com.finalProject.ali.users.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private String userId;
    private String password;
    private String email;
    private String name;
    private String phone;
    private String birth;
    private String address;
    private int isLocked;
    private int loginFailCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
