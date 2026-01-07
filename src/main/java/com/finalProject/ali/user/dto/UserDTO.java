package com.finalProject.ali.user.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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

    //프로필 이미지
    private String profileImg;
    //판매자 권한
    private List<String> roles;
    private String status;
}
