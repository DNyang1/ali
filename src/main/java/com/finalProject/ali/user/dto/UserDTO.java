package com.finalProject.ali.user.dto;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    //image
    private String profileImg;
    //권한
    private String role;
}
