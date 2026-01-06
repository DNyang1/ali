package com.finalProject.ali.user.controller;

import com.finalProject.ali.user.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // 데이터만 주고받으므로 RestController가 적합합니다.
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    // 1. 이메일 인증번호 발송 요청
    @PostMapping("/send-auth-code")
    public ResponseEntity<String> sendAuthCode(@RequestParam("email") String email, HttpSession session) {
        try {
            String authCode = emailService.sendVerificationEmail(email);

            // 세션에 인증번호와 이메일 저장
            session.setAttribute("emailAuthCode", authCode);
            session.setAttribute("authEmail", email);

            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    // 2. 인증번호 검증 요청
    @PostMapping("/verify-auth-code")
    public ResponseEntity<String> verifyAuthCode(@RequestParam("code") String code, HttpSession session) {
        String savedCode = (String) session.getAttribute("emailAuthCode");

        if (savedCode != null && savedCode.equals(code)) {
            session.setAttribute("isEmailVerified", true);
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
        }
    }
}