package com.finalProject.ali.user.dto;

import lombok.Data;

@Data
public class SignupRequestDTO {
    private String userId;
    private String password;
    private String email;
    private String name;
    private String phone;
    private String birth;
    private String address;

    // 서비스 계층으로 넘기기 위한 변환 메서드
    public UserDTO toUserDTO() {
        UserDTO dto = new UserDTO();
        dto.setUserId(this.userId);
        dto.setPassword(this.password);
        dto.setEmail(this.email);
        dto.setName(this.name);
        dto.setPhone(this.phone);
        dto.setBirth(this.birth);
        dto.setAddress(this.address);
        return dto;
    }
}