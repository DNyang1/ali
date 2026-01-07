package com.finalProject.ali.mypage.user.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileView {
    private String name;
    private String email;

    private String grade;
    private String defaultAddressName;
    private String companyName;
    public UserProfileView(String name, String email){
        this.name = name;
        this.email = email;
    }
    public static UserProfileView empty() {
        return new UserProfileView("사용자", "user@email.com", null, null, null);
    }
}
