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

    // UserController.java 에 추가

    @GetMapping("/update")
    public String updatePage() {
        return "user/update"; // update.html 반환
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<String> update(@RequestBody UserDTO userDTO, HttpSession session) {
        // 1. 현재 로그인된 세션 정보 가져오기
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser != null) {
            // 2. 보안을 위해 세션의 아이디를 DTO에 강제 세팅 (남의 정보 수정 방지)
            userDTO.setUserId(loginUser.getUserId());

            // 3. DB 업데이트
            userService.updateUserInfo(userDTO);

            // 4. 세션 정보 최신화 (이름 등이 바뀌었을 수 있으므로)
            // 주의: 비밀번호 등은 보안상 세션에 유지하지 않는 것이 좋지만,
            // 현재 구조상 index.html에서 이름을 보여주기 위해 세션 갱신이 필요합니다.
            loginUser.setName(userDTO.getName());
            loginUser.setEmail(userDTO.getEmail());
            loginUser.setPhone(userDTO.getPhone());
            loginUser.setAddress(userDTO.getAddress());
            session.setAttribute("loginUser", loginUser);

            return ResponseEntity.ok("success");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("fail");
    }

}