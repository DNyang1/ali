//package com.finalProject.ali.user.controller;
//
//import com.finalProject.ali.image.service.ImageService;
//import com.finalProject.ali.user.dto.SupplierDTO;
//import com.finalProject.ali.user.dto.UserDTO;
//import com.finalProject.ali.user.service.EmailService;
//import com.finalProject.ali.user.service.UserService;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.session.SessionRegistry;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.security.Principal; // Security 인증 정보
//import java.util.Map;
//
//@Controller
//@RequestMapping("/user")
//public class UserController {
//
//    @Autowired
//    private UserService userService;
//    @Autowired
//    private SessionRegistry sessionRegistry;
//    @Autowired
//    private EmailService emailService;
//    @Autowired
//    private AuthenticationManager authenticationManager;
//    @Autowired
//    private ImageService imageService;
//
//    // 메인 페이지
//    @GetMapping("/")
//    public String indexPage() {
//        return "index/index";
//    }
//
//    // 회원가입 페이지
//    @GetMapping("/register")
//    public String registerPage() {
//        return "user/register";
//    }
//
//    // 로그인 페이지
//    @GetMapping("/login")
//    public String loginPage() {
//        return "user/login";
//    }
//
//    // [회원가입 로직] - 변수명 변경 없음
//    @PostMapping("/signup")
//    @ResponseBody
//    public ResponseEntity<String> signup(@RequestBody UserDTO userDTO, HttpSession session) {
//        Boolean isVerified = (Boolean) session.getAttribute("isEmailVerified");
//        String authEmail = (String) session.getAttribute("authEmail");
//
//        if (isVerified == null || !isVerified || !userDTO.getEmail().equals(authEmail)) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이메일 인증이 필요합니다.");
//        }
//
//        userService.register(userDTO);
//
//        session.removeAttribute("emailAuthCode");
//        session.removeAttribute("isEmailVerified");
//        session.removeAttribute("authEmail");
//
//        return ResponseEntity.ok("회원가입 성공");
//    }
//
//    // [이메일 인증 발송]
//    @PostMapping("/send-auth-code")
//    @ResponseBody
//    public ResponseEntity<String> sendAuthCode(@RequestParam("email") String email, HttpSession session) {
//        try {
//            String authCode = emailService.sendVerificationEmail(email);
//            session.setAttribute("emailAuthCode", authCode);
//            session.setAttribute("authEmail", email);
//            session.setAttribute("isEmailVerified", false);
//            return ResponseEntity.ok("success");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
//        }
//    }
//
//    // [이메일 인증 확인]
//    @PostMapping("/verify-auth-code")
//    @ResponseBody
//    public ResponseEntity<String> verifyAuthCode(@RequestParam("code") String code, HttpSession session) {
//        String serverCode = (String) session.getAttribute("emailAuthCode");
//        if (serverCode != null && serverCode.equals(code)) {
//            session.setAttribute("isEmailVerified", true);
//            return ResponseEntity.ok("success");
//        } else {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
//        }
//    }
//
//    // ✅ [로그인]
//    @PostMapping("/login")
//    @ResponseBody
//    public ResponseEntity<?> login(@RequestBody UserDTO loginDTO, HttpSession session) {
//        // 1. 변수명 깔끔하게 DTO에서 가져오기
//        String userId = loginDTO.getUserId();
//        String password = loginDTO.getPassword();
//
//        // 2. [핵심] Security 인증 전에 '정지 상태' 먼저 확인 (예전 로직 부활)
//        // 이렇게 하면 Exception 처리가 꼬일 일 없이 사유가 확실하게 뜹니다.
//        UserDTO checkUser = userService.findByUserId(userId);
//        if (checkUser != null && "SUSPENDED".equals(checkUser.getStatus())) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body("정지된 계정입니다. 사유: " + checkUser.getSuspensionReason());
//        }
//
//        try {
//            // 3. Security 인증 진행
//            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(userId, password);
//            Authentication authentication = authenticationManager.authenticate(authRequest);
//
//            // 4. Security Context 저장
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
//            sessionRegistry.registerNewSession(session.getId(), authentication.getPrincipal());
//
//            // 5. 세션 유지 (HTML 호환용)
//            session.setAttribute("loginUser", checkUser); // 위에서 조회한 checkUser 재사용
//
//            Map<String, String> response = new java.util.HashMap<>();
//            response.put("status", "success");
//
//            // 권한별 리다이렉트
//            String mainRole = "ROLE_USER";
//            if (checkUser.getRoles() != null && !checkUser.getRoles().isEmpty()) {
//                if (checkUser.getRoles().contains("ROLE_ADMIN")) mainRole = "ROLE_ADMIN";
//                else if (checkUser.getRoles().contains("ROLE_SUPPLIER")) mainRole = "ROLE_SUPPLIER";
//                else mainRole = checkUser.getRoles().get(0);
//            }
//            response.put("role", mainRole);
//
//            return ResponseEntity.ok(response);
//
//        } catch (AuthenticationException e) {
//            // 비번 틀림 등 나머지 에러 처리
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 일치하지 않습니다.");
//        }
//    }
//
//    // 로그아웃
//    @GetMapping("/logout")
//    public String logout(HttpSession session) {
//        session.invalidate();
//        return "redirect:/";
//    }
//
//    // --- 👇 여기서부터 수정 (변수명 유지하면서 Security 적용) ---
//
//    // 회원정보 수정 페이지
//    @GetMapping("/update")
//    public String updatePage(HttpSession session) {
//        // [중요] 세션이 없으면 로그인 페이지로 (HTML 렌더링 오류 방지)
//        if (session.getAttribute("loginUser") == null) {
//            return "redirect:/user/login";
//        }
//        return "user/update";
//    }
//
//    // 회원정보 수정 처리
//    @PostMapping("/update")
//    @ResponseBody
//    public ResponseEntity<String> update(
//            @RequestPart("userData") UserDTO userDTO, // JS 변수명 userData 유지
//            @RequestPart(value = "profileFile", required = false) MultipartFile profileFile, // JS 변수명 profileFile 유지
//            HttpSession session,
//            Principal principal) { // Security ID 확인용
//
//        // 1. 현재 로그인된 유저 정보 가져오기 (Security 기준)
//        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("fail");
//        String currentUserId = principal.getName();
//        UserDTO loginUser = userService.findByUserId(currentUserId);
//
//        // 2. 프로필 이미지 처리
//        if (profileFile != null && !profileFile.isEmpty()) {
//            if (loginUser.getProfileImg() != null) {
//                imageService.deleteActualFile(loginUser.getProfileImg());
//            }
//            String uploadedPath = imageService.uploadImage(profileFile, "profiles");
//            userDTO.setProfileImg(uploadedPath);
//        } else {
//            userDTO.setProfileImg(loginUser.getProfileImg());
//        }
//
//        // 3. 정보 업데이트
//        userDTO.setUserId(currentUserId);
//        userService.updateUserInfo(userDTO);
//
//        // 4. [중요] 세션 갱신 (HTML 상단의 프로필 사진 등이 바로 바뀌도록)
//        UserDTO updatedUser = userService.findByUserId(currentUserId);
//        session.setAttribute("loginUser", updatedUser);
//
//        return ResponseEntity.ok("success");
//    }
//
//    // 프로필 이미지 삭제
//    @PostMapping("/delete_profile_img")
//    @ResponseBody
//    public ResponseEntity<String> deleteProfileImg(HttpSession session, Principal principal) {
//        try {
//            if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("fail");
//
//            UserDTO loginUser = userService.findByUserId(principal.getName());
//
//            if (loginUser.getProfileImg() != null) {
//                imageService.deleteActualFile(loginUser.getProfileImg());
//            }
//            loginUser.setProfileImg(null);
//            userService.updateUserInfo(loginUser);
//
//            // [중요] 세션 갱신
//            session.setAttribute("loginUser", loginUser);
//            return ResponseEntity.ok("success");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
//        }
//    }
//
//    // 판매자/구매자 전환
//    @GetMapping("/switch-role")
//    public String switchRole(
//            @RequestParam(value = "reapply", required = false) Boolean reapply, // 파라미터명 reapply 유지
//            HttpSession session,
//            Model model,
//            Principal principal) {
//
//        if (principal == null) return "redirect:/user/login";
//
//        SupplierDTO supplier = userService.getSupplierInfo(principal.getName());
//
//        if (supplier == null) return "user/supplier_register";
//
//        String status = supplier.getStatus();
//
//        if ("APPROVED".equals(status)) {
//            session.setAttribute("supplierInfo", supplier);
//            return "redirect:/mypage/supplier/dashboard";
//        }
//        else if ("REJECTED".equals(status) && Boolean.TRUE.equals(reapply)) {
//            model.addAttribute("supplier", supplier);
//            return "user/supplier_register";
//        }
//        else {
//            model.addAttribute("status", status);
//            model.addAttribute("supplier", supplier);
//            return "user/supplier_status";
//        }
//    }
//
//    // 판매자 신청
//    @PostMapping("/supplier-signup")
//    @ResponseBody
//    public ResponseEntity<String> supplierSignup(@RequestBody SupplierDTO supplierDTO, Principal principal) {
//        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("fail");
//
//        supplierDTO.setUserId(principal.getName());
//        supplierDTO.setSupplierId("s_" + principal.getName());
//        userService.registerSupplier(supplierDTO);
//        return ResponseEntity.ok("success");
//    }
//
//    // 비밀번호 변경 페이지
//    @GetMapping("/update_password")
//    public String updatePasswordPage() {
//        return "user/update_password";
//    }
//
//    // 비밀번호 변경 처리
//    @PostMapping("/update_password")
//    @ResponseBody
//    public ResponseEntity<String> updatePassword(
//            @RequestParam("currentPassword") String currentPassword, // JS 파라미터명 유지
//            @RequestParam("newPassword") String newPassword,         // JS 파라미터명 유지
//            Principal principal,
//            HttpSession session) {
//
//        boolean isChanged = userService.changePassword(principal.getName(), currentPassword, newPassword);
//
//        if (isChanged) {
//            session.invalidate(); // 비밀번호 바뀌었으니 재로그인
//            return ResponseEntity.ok("success");
//        } else {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("wrong_password");
//        }
//    }
//
//    // 마이페이지
//    @GetMapping("/setting")
//    public String setting(Model model, Principal principal) {
//        if (principal == null) return "redirect:/user/login";
//
//        UserDTO loginUser = userService.findByUserId(principal.getName());
//        model.addAttribute("user", loginUser);
//        return "user/setting";
//    }
//
//    // --- 비로그인 영역 (그대로 유지) ---
//
//    @GetMapping("/find_id")
//    public String findIdPage() { return "user/find_id"; }
//
//    @PostMapping("/find_id")
//    @ResponseBody
//    public ResponseEntity<String> findId(@RequestBody Map<String, String> data) {
//        String name = data.get("name");
//        String type = data.get("type");
//        String value = data.get("value");
//        String userId = userService.findId(name, type, value);
//
//        if (userId != null) {
//            int length = userId.length();
//            String maskedId = (length > 4)
//                    ? userId.substring(0, 4) + "*".repeat(length - 4)
//                    : userId.substring(0, 1) + "*".repeat(length - 1);
//            return ResponseEntity.ok(maskedId);
//        }
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("not_found");
//    }
//
//    @GetMapping("/reset_pw")
//    public String resetPwPage() { return "user/reset_pw"; }
//
//    @PostMapping("/send_reset_link")
//    @ResponseBody
//    public ResponseEntity<String> sendResetLink(@RequestBody Map<String, String> data) {
//        String userId = data.get("userId");
//        String email = data.get("email");
//        if (userService.checkUserForReset(userId, email)) {
//            userService.processForgotPassword(userId, email);
//            return ResponseEntity.ok("success");
//        }
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("not_found");
//    }
//
//    @GetMapping("/reset_pw_confirm")
//    public String confirmResetToken(@RequestParam("token") String token, Model model) {
//        String userId = userService.verifyResetToken(token);
//        if (userId != null) {
//            model.addAttribute("userId", userId);
//            model.addAttribute("token", token);
//            return "user/reset_pw_form";
//        }
//        return "redirect:/user/login?error=invalid_token";
//    }
//
//    @PostMapping("/reset_pw_final")
//    @ResponseBody
//    public ResponseEntity<String> resetPwFinal(@RequestParam("token") String token,
//                                               @RequestParam("userId") String userId,
//                                               @RequestParam("newPassword") String newPassword) {
//        boolean success = userService.resetPasswordWithToken(token, userId, newPassword);
//        return success ? ResponseEntity.ok("success") : ResponseEntity.badRequest().body("fail");
//    }
//}