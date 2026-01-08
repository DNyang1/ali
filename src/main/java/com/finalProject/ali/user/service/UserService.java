package com.finalProject.ali.user.service;

import com.finalProject.ali.user.dao.UserDAO;
import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService implements org.springframework.security.core.userdetails.UserDetailsService {
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;


    // 회원가입
    @Transactional
    public void register(UserDTO userDTO) {
        String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
        userDTO.setPassword(encodedPassword);

        userDAO.insertUser(userDTO); // users 테이블 저장
        userDAO.insertUserRole(userDTO.getUserId(), "ROLE_USER"); // user_roles 테이블에 권한 저장
    }

    public UserDTO findByUserId(String userId) {
        return userDAO.findByUserId(userId);
    }
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserDTO user = userDAO.findByUserId(userId);
        if (user == null) throw new UsernameNotFoundException(userId);

        if ("SUSPENDED".equals(user.getStatus())) {
            // Spring Security의 LockedException 등을 활용하거나, 메시지를 담아 던짐
            throw new org.springframework.security.authentication.LockedException("정지된 계정입니다. 사유: " + user.getSuspensionReason());
        }

        // 이제 roles가 이미 List<String>으로 들어있으므로 split 필요 없음!
        List<String> roles = user.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = List.of("ROLE_USER"); // 안전장치
        }

        return User.builder()
                .username(user.getUserId())
                .password(user.getPassword())
                .authorities(roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()))
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
        UserDTO user = userDAO.findByUserId(userId);

        if (user == null) {
            System.out.println("❌ [비밀번호 변경 실패] 유저를 찾을 수 없음: " + userId);
            return false;
        }

        // 디버깅 로그 (테스트 후 삭제하세요)
        System.out.println("🔍 [비밀번호 변경 시도]");
        System.out.println("   - 사용자 ID: " + userId);
        System.out.println("   - 입력한 현재 비번: " + currentPassword);
        System.out.println("   - DB 암호화된 비번: " + user.getPassword());

        // 2. 현재 비밀번호 일치 여부 확인
        boolean matches = passwordEncoder.matches(currentPassword, user.getPassword());
        System.out.println("   - 일치 여부: " + matches);

        if (matches) {
            return updatePassword(userId, newPassword);
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

    public List<SupplierDTO> getPendingSuppliers() {
        // UserDAO를 통해 PENDING 상태인 공급자 리스트 조회
        return userDAO.findPendingSuppliers();
    }

    @Transactional
    public void approveSupplier(String supplierId, String userId) {
        userDAO.updateSupplierStatus(supplierId, "APPROVED", null);

        // 이미 판매자 권한이 있는지 체크 후 없으면 추가 (중복 insert 방지 로직은 SQL이나 여기서 처리)
        // 간단하게는 try-catch나 DAO의 INSERT IGNORE 사용 가능
        try {
            userDAO.insertUserRole(userId, "ROLE_SUPPLIER");
        } catch (Exception e) {
            // 이미 권한이 있으면 패스
        }
    }
    // 판매자 등록 상태
    public void updateSupplierStatus(String supplierId, String status, String memo) {
        userDAO.updateSupplierStatus(supplierId, status, memo);
    }

    // 전체 회원 조회
    public List<UserDTO> getAllUsers() {
        return userDAO.findAllUsers();
    }
    // 계정 상태 변경 (정지/해제)
    public void updateUserStatus(String userId, String status, String reason) {
        userDAO.updateUserStatus(userId, status, reason);
    }
    // 권한 변경 (기존 로직 활용)
    @Transactional
    public void changeUserRole(String userId, String roleName, boolean isAdd) {
        if (isAdd) {
            userDAO.insertUserRole(userId, roleName);
        } else {
            userDAO.deleteUserRole(userId, roleName);
        }
    }

    @Transactional
    public void setAuthority(String userId, String targetRole) {
        // 1. 일단 기본적으로 ROLE_USER는 무조건 있어야 함 (없으면 추가)
        try {
            userDAO.insertUserRole(userId, "ROLE_USER");
        } catch (Exception e) {
        }

        // 2. 관리자가 선택한 권한에 따라 처리
        if ("ROLE_ADMIN".equals(targetRole)) {
            // 관리자로 승격 시: ROLE_ADMIN 추가
            userDAO.insertUserRole(userId, "ROLE_ADMIN");
        } else if ("ROLE_SUPPLIER".equals(targetRole) || "BUYER,SUPPLIER".equals(targetRole)) {
            // 판매자로 변경 시: ROLE_SUPPLIER 추가, (관리자 권한은 뺄 수도 있음 정책에 따라)
            userDAO.insertUserRole(userId, "ROLE_SUPPLIER");
            userDAO.deleteUserRole(userId, "ROLE_ADMIN"); // 예: 판매자는 관리자 권한 회수
        } else if ("ROLE_USER".equals(targetRole)) {
            // 일반 유저로 강등 시: 나머지 권한 삭제
            userDAO.deleteUserRole(userId, "ROLE_SUPPLIER");
            userDAO.deleteUserRole(userId, "ROLE_ADMIN");
            userDAO.deleteSupplier(userId);
        }


    }

}