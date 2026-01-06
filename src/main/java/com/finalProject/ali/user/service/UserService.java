package com.finalProject.ali.user.service;

import com.finalProject.ali.user.dao.UserDAO;
import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService implements org.springframework.security.core.userdetails.UserDetailsService {
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;


    // 회원가입
    public void register(UserDTO userDTO) {
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
        userDTO.setPassword(encodedPassword);
        userDAO.insertUser(userDTO);
    }

    public UserDTO findByUserId(String userId) {
        return userDAO.findByUserId(userId);
    }
    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String userId)
            throws org.springframework.security.core.userdetails.UsernameNotFoundException {

        UserDTO user = userDAO.findByUserId(userId);

        if (user == null) {
            throw new org.springframework.security.core.userdetails.UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId);
        }

        // 1. DB에서 가져온 role 값이 있는지 확인 (예: "ROLE_ADMIN,ROLE_USER" 또는 "ROLE_ADMIN")
        String rawRole = user.getRole();
        if (rawRole == null || rawRole.isEmpty()) {
            rawRole = "ROLE_USER"; // 권한이 없으면 기본 유저 권한 부여
        }

        // 2. 콤마(,)로 구분된 권한들을 리스트로 변환
        String[] roles = rawRole.split(",");

        // 3. roles() 대신 authorities()를 사용하여 "ROLE_" 접두사 수동 제어
        // SimpleGrantedAuthority를 사용하면 DB에 있는 문자열 그대로 권한을 부여합니다.
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserId())
                .password(user.getPassword())
                .authorities(java.util.Arrays.stream(roles)
                        .map(role -> {
                            // ROLE_ 접두사가 없으면 붙여줌
                            String roleName = role.trim().startsWith("ROLE_") ? role.trim() : "ROLE_" + role.trim();
                            return new org.springframework.security.core.authority.SimpleGrantedAuthority(roleName);
                        })
                        .toList())
                .build();
    }
    // 로그인 확인
    public UserDTO login(String userId, String rawPassword) {
        UserDTO user = userDAO.findByUserId(userId);

        // 사용자가 존재하고, 암호화된 비번과 입력한 비번이 일치하는지 확인
        if(user != null && passwordEncoder.matches(rawPassword, user.getPassword())) {
            return user;
        }
        return null; // 로그인 실패
    }

    // 업데이트
    public void updateUserInfo(UserDTO userDTO) {
        userDAO.updateUser(userDTO);
    }
    public void updateSupplier(SupplierDTO supplierDTO) {
        userDAO.updateSupplier(supplierDTO);
    }

    // 판매자 정보 가져오기
    public SupplierDTO getSupplierInfo(String userId) {
        return userDAO.findSupplierByUserId(userId);
    }

    // 판매자 최초 등록하기
    public void registerSupplier(SupplierDTO supplierDTO) {
        userDAO.insertSupplier(supplierDTO);
    }

    public boolean changePassword(String userId, String currentPassword, String newPassword) {
        // 1. DB에서 현재 유저 정보 가져오기
        UserDTO user = userDAO.getUserById(userId);

        // 2. 현재 비밀번호 일치 여부 확인
        if (passwordEncoder.matches(currentPassword, user.getPassword())) {
            return updatePassword(userId, newPassword); // 아래 공통 메서드 호출
        }
        return false; // 비밀번호 불일치
    }
    // 새 비밀번호 암호화 및 업데이트
    public boolean updatePassword(String userId, String newPassword) {
        // 새 비밀번호 암호화
        String password = passwordEncoder.encode(newPassword);

        // DAO에 전달 (Map이나 DTO 활용)
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("password", password);

        return userDAO.updatePassword(params) > 0;
    }

    public String findId(String name, String type, String value) {
        if ("phone".equals(type)) {
            return userDAO.findIdByPhone(name, value);
        } else if ("email".equals(type)) {
            return userDAO.findIdByEmail(name, value);
        }
        return null;
    }

    // 1. 아이디와 이메일 일치 여부 확인 (UserDAO에 추가 필요)
    public boolean checkUserForReset(String userId, String email) {
        UserDTO user = userDAO.findByUserId(userId);
        return user != null && user.getEmail().equals(email);
    }

    // 2. 임시 비밀번호 생성 및 전송 로직
    public void processForgotPassword(String userId, String email) {
        // 8자리의 랜덤 임시 비밀번호 생성 (UUID 활용)
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        // DB의 비밀번호를 임시 비밀번호(암호화 필수)로 변경
        // 기존에 만들어두신 updatePassword 메서드 활용
        updatePassword(userId, tempPassword);

        // 이메일 전송
        String subject = "[ALI 프로젝트] 임시 비밀번호 안내입니다.";
        String text = "안녕하세요, " + userId + "님.\n\n" +
                "요청하신 임시 비밀번호는 다음과 같습니다.\n" +
                "임시 비밀번호: " + tempPassword + "\n\n" +
                "로그인 후 반드시 마이페이지에서 비밀번호를 변경해 주세요.";

        emailService.sendSimpleEmail(email, subject, text);
    }

    // 3. 토큰 검증 및 사용자 ID 반환
    public String verifyResetToken(String token) {
        return userDAO.getUserIdByToken(token, LocalDateTime.now());
    }

    // 4. 새 비밀번호 설정 및 토큰 삭제
    public boolean resetPasswordWithToken(String token, String userId, String newPassword) {
        boolean isUpdated = updatePassword(userId, newPassword);
        if (isUpdated) {
            userDAO.deleteResetToken(token);
        }
        return isUpdated;
    }

    // UserService.java에 추가
    public List<SupplierDTO> getPendingSuppliers() {
        // UserDAO를 통해 PENDING 상태인 공급자 리스트 조회
        return userDAO.findPendingSuppliers();
    }

    @Transactional
    public void approveSupplier(String supplierId, String userId) { // 매개변수 이름을 userId로 인지
        // 1. 상태 업데이트
        userDAO.updateSupplierStatus(supplierId, "APPROVED");

        // 2. 이메일 대신 ID로 유저 정보를 가져오도록 수정
        UserDTO user = userDAO.findByUserId(userId);
        if (user != null) {
            String currentRole = user.getRole();
            if (currentRole == null || !currentRole.contains("SUPPLIER")) {
                // 기존 권한이 null일 경우를 대비해 처리
                String newRole = (currentRole == null || currentRole.isEmpty()) ? "SUPPLIER" : currentRole + ",SUPPLIER";

                userDAO.updateUserRole(userId, newRole);
            }
        }
    }

}