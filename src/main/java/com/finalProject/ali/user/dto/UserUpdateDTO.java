package com.finalProject.ali.user.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String email;
    private String name;
    private String phone;
    private String address;
    private String profileImg;

    public UserDTO toUserDTO(String currentUserId) {
        UserDTO dto = new UserDTO();
        dto.setUserId(currentUserId);
        dto.setEmail(this.email);
        dto.setName(this.name);
        dto.setPhone(this.phone);
        dto.setAddress(this.address);
        dto.setProfileImg(this.profileImg);
        return dto;
    }
}