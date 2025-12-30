package com.finalProject.ali.mypage.user.common.controller;

import org.springframework.ui.Model;

public abstract class BaseUserController {

    protected void addCommonAttributes(Model model) {
        // TODO: 로그인 유저 정보 주입
        // model.addAttribute("loginUser", ...);

        // TODO: 공통 뱃지/카운트
        // model.addAttribute("unreadMessageCount", 0);
    }
}
