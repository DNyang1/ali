package com.finalProject.ali.user.controller;

import com.finalProject.ali.image.service.ImageService;
import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import com.finalProject.ali.user.service.EmailService;
import com.finalProject.ali.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private ImageService imageService; // 새로 만든 서비스 주입


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
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData, HttpSession session) {
        String userId = loginData.get("userId");
        String password = loginData.get("password");

        try {
            // 1. Spring Security 표준 인증 토큰 생성
            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(userId, password);

            // 2. AuthenticationManager를 통한 인증 시도 (이때 DB 비교가 내부적으로 일어남)
            Authentication authentication = authenticationManager.authenticate(authRequest);

            // 3. 인증 성공 시 SecurityContextHolder에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // 4. 세션 레지스트리에 등록
            sessionRegistry.registerNewSession(session.getId(), authentication.getPrincipal());

            // 5. DB에서 유저 정보를 가져와 세션에 저장 (UI 표시용)
            UserDTO user = userService.findByUserId(userId);
            session.setAttribute("loginUser", user);

            Map<String, String> response = new java.util.HashMap<>();
            response.put("status", "success");
            response.put("role", user.getRole() != null ? user.getRole() : "ROLE_USER");

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            // 인증 실패 시 (아이디 없음, 비밀번호 틀림 등)
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
        return "user/update"; // s_update.html 반환
    }


    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<String> update(
            @RequestPart("userData") UserDTO userDTO,
            @RequestPart(value = "profileFile", required = false) MultipartFile profileFile,
            HttpSession session) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("fail");

        // 1. 프로필 이미지 처리 (ImageService 활용)
        if (profileFile != null && !profileFile.isEmpty()) {
            if (loginUser.getProfileImg() != null) {
                imageService.deleteActualFile(loginUser.getProfileImg());
            }
            String uploadedPath = imageService.uploadImage(profileFile, "profiles");
            userDTO.setProfileImg(uploadedPath);
        } else {
            userDTO.setProfileImg(loginUser.getProfileImg());
        }

        // 2. 정보 업데이트 수행
        userDTO.setUserId(loginUser.getUserId());
        userService.updateUserInfo(userDTO);

        // 3. DB에서 최신 정보를 다시 조회하여 세션 갱신
        UserDTO updatedUser = userService.findByUserId(loginUser.getUserId());
        session.setAttribute("loginUser", updatedUser);

        return ResponseEntity.ok("success");
    }

    @PostMapping("/delete_profile_img")
    @ResponseBody
    public ResponseEntity<String> deleteProfileImg(HttpSession session) {
        // 1. 세션에서 현재 로그인된 유저 가져오기
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        // 로그인 안 되어 있으면 실패 응답
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("fail");
        }

        try {

            if (loginUser.getProfileImg() != null) {
                imageService.deleteActualFile(loginUser.getProfileImg());
            }
            // 2. DTO와 세션에서 이미지 경로 제거
            loginUser.setProfileImg(null);
            // 3. DB 업데이트 (수정된 DTO를 서비스로 전달)
            userService.updateUserInfo(loginUser);
            // 4. 세션 최신화
            session.setAttribute("loginUser", loginUser);

            return ResponseEntity.ok("success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    // 판매자와 구매자 전환
    @GetMapping("/switch-role")
    public String switchRole(
            @RequestParam(value = "reapply", required = false) Boolean reapply, // 1. 지역변수(파라미터) 생성
            HttpSession session,
            org.springframework.ui.Model model) {

        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/user/login";

        SupplierDTO supplier = userService.getSupplierInfo(loginUser.getUserId());

        if (supplier == null) return "user/supplier_register";

        String status = supplier.getStatus();

        if ("APPROVED".equals(status)) {
            session.setAttribute("supplierInfo", supplier);
            return "redirect:/mypage/supplier/dashboard";
        }
        // 2. 반려 상태(REJECTED)이면서 사용자가 '재신청' 버튼을 눌러 reapply=true를 보낸 경우
        else if ("REJECTED".equals(status) && Boolean.TRUE.equals(reapply)) {
            model.addAttribute("supplier", supplier); // 기존에 입력했던 정보를 폼에 뿌려주기 위해 전달
            return "user/supplier_register"; // 등록 폼으로 이동
        }
        // 3. 그 외 PENDING이거나, 그냥 REJECTED 상태를 확인하러 들어온 경우
        else {
            model.addAttribute("status", status);
            model.addAttribute("supplier", supplier); // memo 출력을 위해 supplier 객체 전달
            return "user/supplier_status";
        }
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




}