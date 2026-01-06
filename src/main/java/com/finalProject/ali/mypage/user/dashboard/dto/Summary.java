package com.finalProject.ali.mypage.user.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Summary {
    private int activeOrderCount;
    private int pendingPayOrQuoteCount;
    private int cartCount;
    private int unreadMessageCount;

    public static Summary empty() {
        return new Summary(0, 0, 0, 0);
    }
}
