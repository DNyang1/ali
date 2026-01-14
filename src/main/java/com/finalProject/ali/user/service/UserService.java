package com.finalProject.ali.user.service;

import com.finalProject.ali.repository.UserRepository;
import com.finalProject.ali.user.dao.UserDAO;
import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
// ⚠️ 중요: JPA 엔티티는 'UserEntity'라는 별명으로 가져옵니다! (충돌 방지)
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
@RequiredArgsConstructor // final이 붙은 필드만 생성자 주입 (Autowired 대체)
public class UserService implements UserDetailsService {

    private final UserDAO userDAO;
    private final UserRepository userRepository; //JPA
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // 회원가입: MyBatis -> JPA
    @Transactional
    public void register(UserDTO userDto) {
        // 1. 중복 체크 (JPA)
        if (userRepository.existsByUserId(userDto.getUserId())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
//        if (userRepository.existsByEmail(userDto.getEmail())) {
//            throw new RuntimeException("이미 존재하는 이메일입니다.");
//        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());

        // 3. DTO -> Entity 변환 (Builder 사용)
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

        // 4. 저장 (SQL 없이 저장됨)
        userRepository.save(userEntity);

        // 5. 권한 저장 (권한 테이블은 아직 MyBatis라면 유지, JPA로 바꿨다면 여기도 수정 필요)
        // 일단 기존 호환성을 위해 유지
        userDAO.insertUserRole(userDto.getUserId(), "ROLE_USER");
    }

    // --- 아래는 기존 유지 ---

    public UserDTO findByUserId(String userId) {
        return userDAO.findByUserId(userId);
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserDTO user = userDAO.findByUserId(userId);
        if (user == null) throw new UsernameNotFoundException(userId);

        if ("SUSPENDED".equals(user.getStatus())) {
            throw new org.springframework.security.authentication.LockedException("정지된 계정입니다. 사유: " + user.getSuspensionReason());
        }

        List<String> roles = user.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = List.of("ROLE_USER");
        }

        // 여기의 User는 스프링 시큐리티의 User입니다. (import 충돌 주의)
        return org.springframework.security.core.userdetails.User.builder()
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
        if(user != null && passwordEncoder.matches(rawPassword, user.getPassword())) {
            return user;
        }
        return null;
    }

    // 업데이트
    public void updateUserInfo(UserDTO userDTO) {
        userDAO.updateUser(userDTO);
    }
    public void updateSupplier(SupplierDTO supplierDTO) {
        userDAO.updateSupplier(supplierDTO);
    }

    public SupplierDTO getSupplierInfo(String userId) {
        return userDAO.findSupplierByUserId(userId);
    }

    public void registerSupplier(SupplierDTO supplierDTO) {
        userDAO.insertSupplier(supplierDTO);
    }

    public boolean changePassword(String userId, String currentPassword, String newPassword) {
        UserDTO user = userDAO.findByUserId(userId);
        if (user == null) return false;

        if (passwordEncoder.matches(currentPassword, user.getPassword())) {
            return updatePassword(userId, newPassword);
        }
        return false;
    }

    public boolean updatePassword(String userId, String newPassword) {
        String password = passwordEncoder.encode(newPassword);
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

    public boolean checkUserForReset(String userId, String email) {
        UserDTO user = userDAO.findByUserId(userId);
        return user != null && user.getEmail().equals(email);
    }

    public void processForgotPassword(String userId, String email) {
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        updatePassword(userId, tempPassword);
        String subject = "[ALI 프로젝트] 임시 비밀번호 안내입니다.";
        String text = "임시 비밀번호: " + tempPassword;
        emailService.sendSimpleEmail(email, subject, text);
    }

    public String verifyResetToken(String token) {
        return userDAO.getUserIdByToken(token, LocalDateTime.now());
    }

    public boolean resetPasswordWithToken(String token, String userId, String newPassword) {
        boolean isUpdated = updatePassword(userId, newPassword);
        if (isUpdated) {
            userDAO.deleteResetToken(token);
        }
        return isUpdated;
    }

    public List<SupplierDTO> getPendingSuppliers() {
        return userDAO.findPendingSuppliers();
    }

    @Transactional
    public void approveSupplier(String supplierId, String userId) {
        userDAO.updateSupplierStatus(supplierId, "APPROVED", null);
        try {
            userDAO.insertUserRole(userId, "ROLE_SUPPLIER");
        } catch (Exception e) {
        }
    }

    public void updateSupplierStatus(String supplierId, String status, String memo) {
        userDAO.updateSupplierStatus(supplierId, status, memo);
    }

    public List<UserDTO> getAllUsers() {
        return userDAO.findAllUsers();
    }

    public void updateUserStatus(String userId, String status, String reason) {
        userDAO.updateUserStatus(userId, status, reason);
    }

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
            userDAO.deleteSupplier(userId);
        }
    }
}