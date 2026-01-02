package com.finalProject.ali.user.service;

import com.finalProject.ali.user.dao.UserDAO;
import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
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

}