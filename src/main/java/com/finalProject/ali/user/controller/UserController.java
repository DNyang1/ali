package com.finalProject.ali.user.controller;

import com.finalProject.ali.user.dto.UserDTO;
import com.finalProject.ali.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;



    @GetMapping("/register")
    public String registerPage() {
        return "user/register";
    }

    // 페이지 이동: /user/login 호출 시 login.html 반환
    @GetMapping("/login")
    public String loginPage() {
        return "user/login";
    }

    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<String> signup(@RequestBody UserDTO userDTO) {
        userService.register(userDTO);
        return ResponseEntity.ok("회원가입 성공");
    }

    // UserController.java
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<String> login(@RequestBody Map<String, String> loginData, HttpSession session) {
        String userId = loginData.get("userId");
        String password = loginData.get("password");

        // 서비스에서 유저 정보 가져오기
        UserDTO user = userService.login(userId, password);

        if (user != null) {
            // [이 코드가 반드시 있어야 함] 세션에 유저 정보를 저장
            session.setAttribute("loginUser", user);
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("fail");
        }
    }

    // 로그아웃 기능 추가
    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpSession session) {
        session.invalidate(); // 세션 무효화
        return "redirect:/user/index"; // 메인 페이지로 이동
    }

    // 루트(/) 경로 접속 시 index.html 반환
    @GetMapping("/index")
    public String indexPage(HttpSession session) {
        // 세션 정보는 스프링이 자동으로 관리하므로 뷰 이름만 정확히 리턴하면 됩니다.
        return "user/index";
    }
}