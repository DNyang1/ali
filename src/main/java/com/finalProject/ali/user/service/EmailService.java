package com.finalProject.ali.user.service;

import com.finalProject.ali.user.dao.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // 이메일 발송 공통 메서드
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("hjin112255@gmail.com"); // application.properties에 설정한 계정
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
    public String sendVerificationEmail(String to) {
        // 1. 6자리 랜덤 인증번호 생성
        String authCode = String.valueOf((int)(Math.random() * 899999) + 100000);

        // 2. 메일 내용 작성
        String subject = "[ALI] 회원가입 인증번호 안내";
        String text = "안녕하세요, ALI 서비스입니다.\n\n" +
                "회원가입을 위한 인증번호는 다음과 같습니다.\n" +
                "인증번호: " + authCode + "\n\n" +
                "해당 번호를 인증 창에 입력해 주세요.";

        // 3. 메일 발송
        sendSimpleEmail(to, subject, text);

        return authCode; // 생성된 코드를 컨트롤러에서 세션에 저장할 수 있도록 반환
    }
}