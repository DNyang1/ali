package com.finalProject.ali.user.service;

import com.finalProject.ali.user.dao.UserDAO;
import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    @Autowired
    private UserDAO userDAO;

    @Autowired private BCryptPasswordEncoder passwordEncoder;


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

    // 2. 비밀번호 재설정 전 사용자 확인 (ID와 이메일이 일치하는지)
    public boolean checkUserForReset(String userId, String email) {
        UserDTO user = userDAO.findByUserId(userId);
        return user != null && user.getEmail().equals(email);
    }




}