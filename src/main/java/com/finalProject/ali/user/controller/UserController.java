package com.finalProject.ali.user.controller;

import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import com.finalProject.ali.user.service.EmailService;
import com.finalProject.ali.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private SessionRegistry sessionRegistry;
    @Autowired
    private EmailService emailService;


    @GetMapping("/")
    public String indexPage(HttpSession session) {
        return "/index/index";
    }


    @GetMapping("/register")
    public String registerPage() {
        return "user/register";
    }


    @GetMapping("/login")
    public String loginPage() {
        return "user/login";
    }

    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<String> signup(@RequestBody UserDTO userDTO, HttpSession session) {
        // 세션에서 인증 여부 확인
        Boolean isVerified = (Boolean) session.getAttribute("isEmailVerified");
        String authEmail = (String) session.getAttribute("authEmail");

        if (isVerified == null || !isVerified || !userDTO.getEmail().equals(authEmail)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이메일 인증이 필요합니다.");
        }

        userService.register(userDTO);

        // 가입 성공 후 세션 정보 정리
        session.removeAttribute("emailAuthCode");
        session.removeAttribute("isEmailVerified");
        session.removeAttribute("authEmail");

        return ResponseEntity.ok("회원가입 성공");
    }
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<String> login(@RequestBody Map<String, String> loginData, HttpSession session, HttpServletRequest request) {
        String userId = loginData.get("userId");
        String password = loginData.get("password");

        // 서비스에서 유저 정보 가져오기
        UserDTO user = userService.login(userId, password);

        // UserController.java의 login 메서드 수정 부분
        if (user != null) {
            session.setAttribute("loginUser", user);

            String principal = user.getUserId();

            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(principal, null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

            SecurityContextHolder.getContext().setAuthentication(token);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // 세션 레지스트리에 아이디(String) 등록
            sessionRegistry.registerNewSession(session.getId(), token.getPrincipal());

            return ResponseEntity.ok("success");

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("fail");
        }
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpSession session) {
        session.invalidate(); // 세션 무효화
        return "redirect:/"; // 메인 페이지로 이동
    }


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

    // 판매자와 구매자 전환
    @GetMapping("/switch-role")
    public String switchRole(HttpSession session) {
        // 1. 세션에서 현재 로그인 유저 가져오기
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null) {
            System.out.println("DEBUG: 세션에 loginUser가 없음!");
            return "redirect:/user/login";
        }
        // 2. 서비스로 판매자 정보(supplier)가 있는지 조회
        SupplierDTO supplier = userService.getSupplierInfo(loginUser.getUserId());

        if (supplier == null) {
            // 3. 판매자 정보가 없으면 등록 페이지로 이동
            return "user/supplier_register";
        }

        // 4. 이미 판매자라면 판매자 전용 메인 페이지로 이동
        session.setAttribute("supplierInfo", supplier);
        return "redirect:/mypage/supplier/dashboard";
    }

    @PostMapping("/supplier-signup")
    @ResponseBody
    public ResponseEntity<String> supplierSignup(@RequestBody SupplierDTO supplierDTO, HttpSession session) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser != null) {
            // 세션의 userId를 SupplierDTO에 심어줌
            supplierDTO.setUserId(loginUser.getUserId());
            // supplier_id 생성 (예: s_아이디)
            supplierDTO.setSupplierId("s_" + loginUser.getUserId());
            // 서비스 호출하여 DB 저장 (userService.registerSupplier)
            userService.registerSupplier(supplierDTO);

            return ResponseEntity.ok("success");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("fail");
    }

    // 비밀번호 수정
    @GetMapping("/update_password")
    public String updatePasswordPage() {
        return "user/update_password";
    }
    @PostMapping("/update_password")
    @ResponseBody
    public ResponseEntity<String> updatePassword(@RequestParam String currentPassword,
                                                 @RequestParam String newPassword,
                                                 HttpSession session) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("fail");

        boolean isChanged = userService.changePassword(loginUser.getUserId(), currentPassword, newPassword);

        if (isChanged) {
            // 비밀번호가 바뀌었으므로 세션을 무효화하거나 업데이트 권장
            session.removeAttribute("loginUser");
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("wrong_password");
        }
    }

    // 마이페이지
    @GetMapping("/setting")
    public String setting(HttpSession session, org.springframework.ui.Model model) {
        // 1. 세션에서 로그인된 유저 정보 확인
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/user/login"; // 로그인 안 되어 있으면 로그인 페이지로
        }

        // 2. 화면에 유저 정보를 뿌려주기 위해 모델에 담기
        model.addAttribute("user", loginUser);

        return "user/setting";
    }


    @GetMapping("/find_id")
    public String findIdPage() {
        return "user/find_id"; // templates/user/find_id.html 호출
    }

    @PostMapping("/find_id")
    @ResponseBody
    public ResponseEntity<String> findId(@RequestBody Map<String, String> data) {
        String name = data.get("name");
        String type = data.get("type"); // "phone" 또는 "email"
        String value = data.get("value");

        String userId = userService.findId(name, type, value);

        if (userId != null) {
            int length = userId.length();
            String maskedId = (length > 4)
                    ? userId.substring(0, 4) + "*".repeat(length - 4)
                    : userId.substring(0, 1) + "*".repeat(length - 1);
            return ResponseEntity.ok(maskedId);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("not_found");
    }

    @GetMapping("/reset_pw")
    public String resetPwPage() {
        return "user/reset_pw"; // templates/user/reset_pw.html 호출
    }
    @PostMapping("/reset_pw")
    @ResponseBody
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> data) {
        String userId = data.get("userId");
        String newPassword = data.get("newPassword");

        boolean success = userService.updatePassword(userId, newPassword);

        return success ? ResponseEntity.ok("success") : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
    }







    @PostMapping("/send_reset_link")
    @ResponseBody
    public ResponseEntity<String> sendResetLink(@RequestBody Map<String, String> data) {
        String userId = data.get("userId");
        String email = data.get("email");

        // 아이디와 이메일이 일치하는 유저가 있는지 확인
        if (userService.checkUserForReset(userId, email)) {
            userService.processForgotPassword(userId, email);
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("not_found");
        }
    }

    @GetMapping("/reset_pw_confirm")
    public String confirmResetToken(@RequestParam String token, HttpSession session, org.springframework.ui.Model model) {
        // 토큰 검증
        String userId = userService.verifyResetToken(token);

        if (userId != null) {
            // 유효한 토큰이면 세션에 인증 정보를 담거나 모델에 userId를 전달
            model.addAttribute("userId", userId);
            model.addAttribute("token", token);
            return "user/reset_pw_form"; // 새 비밀번호를 입력할 새로운 HTML 페이지
        } else {
            return "redirect:/user/login?error=invalid_token";
        }

    }
    // 새 비밀번호 실제 반영 API
    @PostMapping("/reset_pw_final")
    @ResponseBody
    public ResponseEntity<String> resetPwFinal(@RequestParam String token,
                                               @RequestParam String userId,
                                               @RequestParam String newPassword) {
        boolean success = userService.resetPasswordWithToken(token, userId, newPassword);
        return success ? ResponseEntity.ok("success") : ResponseEntity.badRequest().body("fail");
    }

    // 1. 이메일 인증번호 발송 요청
    @PostMapping("/send-auth-code")
    @ResponseBody
    public ResponseEntity<String> sendAuthCode(@RequestParam String email, HttpSession session) {
        try {
            String authCode = emailService.sendVerificationEmail(email);

            // 세션에 인증번호 저장 (유효기간 설정을 위해 생성 시간도 함께 저장 가능)
            session.setAttribute("emailAuthCode", authCode);
            session.setAttribute("authEmail", email); // 인증 시도한 이메일 고정

            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    // 2. 인증번호 검증 요청
    @PostMapping("/verify-auth-code")
    @ResponseBody
    public ResponseEntity<String> verifyAuthCode(@RequestParam String code, HttpSession session) {
        String savedCode = (String) session.getAttribute("emailAuthCode");

        if (savedCode != null && savedCode.equals(code)) {
            // 인증 성공 시 세션에 마킹 (최종 회원가입 단계에서 체크)
            session.setAttribute("isEmailVerified", true);
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
        }
    }




}