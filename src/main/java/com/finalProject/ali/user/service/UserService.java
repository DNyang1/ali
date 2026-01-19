package com.finalProject.ali.user.service;

import com.finalProject.ali.repository.UserRepository;
import com.finalProject.ali.user.dao.UserDAO;
import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import com.finalProject.ali.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserDAO userDAO;
    private final UserRepository userRepository; // JPA
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ===================================================================================
    //  [SECTION 1] 인증 및 로그인 (Authentication & Security)
    // ===================================================================================

    // Spring Security 필수 구현체
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserDTO user = userDAO.findByUserId(userId);
        if (user == null) throw new UsernameNotFoundException(userId);

        // 정지된 계정 차단
        if ("SUSPENDED".equals(user.getStatus())) {
            throw new org.springframework.security.authentication.LockedException("정지된 계정입니다. 사유: " + user.getSuspensionReason());
        }

        List<String> roles = user.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = List.of("ROLE_USER");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserId())
                .password(user.getPassword())
                .authorities(roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()))
                .build();
    }

    // 수동 로그인 확인 (Security 미사용 시 혹은 보조용)
    public UserDTO login(String userId, String rawPassword) {
        UserDTO user = userDAO.findByUserId(userId);
        if(user != null && passwordEncoder.matches(rawPassword, user.getPassword())) {
            return user;
        }
        return null;
    }

    // ===================================================================================
    //  [SECTION 2] 일반 회원 서비스 (User Basic Service)
    //  : 가입, 조회, 정보수정, 아이디 찾기
    // ===================================================================================

    // 회원가입 (JPA + MyBatis 권한 부여)
    @Transactional
    public void register(UserDTO userDto) {
        // 1. 중복 체크
        if (userRepository.existsByUserId(userDto.getUserId())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        // 2. 비밀번호 암호화 및 Entity 변환
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        User userEntity = User.builder()
                .userId(userDto.getUserId())
                .password(encodedPassword)
                .email(userDto.getEmail())
                .name(userDto.getName())
                .phone(userDto.getPhone())
                .birth(userDto.getBirth())
                .address(userDto.getAddress())
                .status("ACTIVE")
                .build();

        // 3. 저장
        userRepository.save(userEntity);

        // 4. 권한 부여 (기본 유저)
        userDAO.insertUserRole(userDto.getUserId(), "ROLE_USER");
    }

    // 회원 정보 조회
    public UserDTO findByUserId(String userId) {
        return userDAO.findByUserId(userId);
    }

    // 회원 정보 수정
    public void updateUserInfo(UserDTO userDTO) {
        userDAO.updateUser(userDTO);
    }

    // 아이디 찾기
    public String findId(String name, String type, String value) {
        if ("phone".equals(type)) {
            return userDAO.findIdByPhone(name, value);
        } else if ("email".equals(type)) {
            return userDAO.findIdByEmail(name, value);
        }
        return null;
    }

    // ===================================================================================
    //  [SECTION 3] 비밀번호 및 계정 보안 (Password & Account Security)
    //  : 비밀번호 변경, 찾기(임시발급), 토큰 검증
    // ===================================================================================

    // 비밀번호 변경 (마이페이지)
    public boolean changePassword(String userId, String currentPassword, String newPassword) {
        UserDTO user = userDAO.findByUserId(userId);
        if (user == null) return false;

        if (passwordEncoder.matches(currentPassword, user.getPassword())) {
            return updatePassword(userId, newPassword);
        }
        return false;
    }

    // 비밀번호 업데이트 (공통 내부 로직)
    public boolean updatePassword(String userId, String newPassword) {
        String password = passwordEncoder.encode(newPassword);
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("password", password);
        return userDAO.updatePassword(params) > 0;
    }

    // 비밀번호 찾기 전 검증
    public boolean checkUserForReset(String userId, String email) {
        UserDTO user = userDAO.findByUserId(userId);
        return user != null && user.getEmail().equals(email);
    }

    // 임시 비밀번호 발송
    public void processForgotPassword(String userId, String email) {
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        updatePassword(userId, tempPassword);
        String subject = "[ALI 프로젝트] 임시 비밀번호 안내입니다.";
        String text = "임시 비밀번호: " + tempPassword;
        emailService.sendSimpleEmail(email, subject, text);
    }

    // 비밀번호 재설정 토큰 검증
    public String verifyResetToken(String token) {
        return userDAO.getUserIdByToken(token, LocalDateTime.now());
    }

    // 토큰을 이용한 비밀번호 재설정 완료
    public boolean resetPasswordWithToken(String token, String userId, String newPassword) {
        boolean isUpdated = updatePassword(userId, newPassword);
        if (isUpdated) {
            userDAO.deleteResetToken(token);
        }
        return isUpdated;
    }

    // ===================================================================================
    //  [SECTION 4] 판매자 서비스 (Supplier Service)
    //  : 입점 신청, 정보 조회/수정
    // ===================================================================================

    // 입점 신청
    public void registerSupplier(SupplierDTO supplierDTO) {
        userDAO.insertSupplier(supplierDTO);
    }

    // 판매자 정보 조회
    public SupplierDTO getSupplierInfo(String userId) {
        return userDAO.findSupplierByUserId(userId);
    }

    // 판매자 정보 수정
    public void updateSupplier(SupplierDTO supplierDTO) {
        userDAO.updateSupplier(supplierDTO);
    }

    // ===================================================================================
    //  [SECTION 5] 관리자 기능 (Admin Service)
    //  : 회원 관리, 판매자 승인/반려, 권한 제어
    // ===================================================================================

    // 전체 회원 조회
    public List<UserDTO> getAllUsers() {
        return userDAO.findAllUsers();
    }

    // 회원 상태 변경 (정지/해제)
    public void updateUserStatus(String userId, String status, String reason) {
        userDAO.updateUserStatus(userId, status, reason);
    }

    // 승인 대기중인 판매자 목록
    public List<SupplierDTO> getPendingSuppliers() {
        return userDAO.findPendingSuppliers();
    }

    // 판매자 승인 처리
    @Transactional
    public void approveSupplier(String supplierId, String userId) {
        userDAO.updateSupplierStatus(supplierId, "APPROVED", null);
        try {
            userDAO.insertUserRole(userId, "ROLE_SUPPLIER");
        } catch (Exception e) {
            // 이미 권한이 있는 경우 무시
        }
    }

    // 판매자 반려 또는 상태 변경
    public void updateSupplierStatus(String supplierId, String status, String memo) {
        userDAO.updateSupplierStatus(supplierId, status, memo);
    }

    // 권한 강제 변경 (관리자용)
    @Transactional
    public void setAuthority(String userId, String targetRole) {
        // 일단 기본 유저 권한은 보장
        try {
            userDAO.insertUserRole(userId, "ROLE_USER");
        } catch (Exception e) {}

        if ("ROLE_ADMIN".equals(targetRole)) {
            userDAO.insertUserRole(userId, "ROLE_ADMIN");
        } else if ("ROLE_SUPPLIER".equals(targetRole) || "BUYER,SUPPLIER".equals(targetRole)) {
            userDAO.insertUserRole(userId, "ROLE_SUPPLIER");
            userDAO.deleteUserRole(userId, "ROLE_ADMIN");
        } else if ("ROLE_USER".equals(targetRole)) {
            userDAO.deleteUserRole(userId, "ROLE_SUPPLIER");
            userDAO.deleteUserRole(userId, "ROLE_ADMIN");
            userDAO.deleteSupplier(userId); // 판매자 정보도 삭제할지 정책에 따라 결정
        }
    }

    // 권한 추가/삭제 헬퍼 메소드
    @Transactional
    public void changeUserRole(String userId, String roleName, boolean isAdd) {
        if (isAdd) {
            userDAO.insertUserRole(userId, roleName);
        } else {
            userDAO.deleteUserRole(userId, roleName);
        }
    }

    // [관리자] 회원 목록 조회 (페이징 + 검색)
    public List<UserDTO> getUsersWithPaging(com.finalProject.ali.admin.dto.UserSearchDTO searchDTO) {
        return userDAO.findAllUsersWithPaging(searchDTO);
    }

    // [관리자] 전체 회원 수 조회 (페이징 계산용)
    public int getUsersCount(com.finalProject.ali.admin.dto.UserSearchDTO searchDTO) {
        return userDAO.countUsersWithPaging(searchDTO);
    }
}