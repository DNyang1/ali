package com.finalProject.ali.user.dao;

import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserDAO {
    void insertUser(UserDTO userDTO); // 회원가입

    UserDTO findByUserId(String userId);

    // 회원 정보 수정
    void updateUser(UserDTO userDTO);
    void updateSupplier(SupplierDTO supplierDTO);
    // 비밀번호 변경
    UserDTO getUserById(String userId);
    int updatePassword(Map<String, String> params);

    // 구매자/판매자 찾기
    SupplierDTO findSupplierByUserId(String userId);
    void insertSupplier(SupplierDTO supplierDTO);

    String findIdByPhone(@Param("name") String name, @Param("phone") String phone);
    String findIdByEmail(@Param("name") String name, @Param("email") String email);

    // 토큰 정보를 DB에 저장합니다.
    void insertResetToken(@Param("token") String token, @Param("userId") String userId, @Param("expiryDate") java.time.LocalDateTime expiryDate);

    // 만료되지 않은 토큰이 있는지 확인하고 해당 유저의 ID를 가져옵니다.
    String getUserIdByToken(@Param("token") String token, @Param("now") java.time.LocalDateTime now);

    // 비밀번호 변경 완료 후 사용한 토큰은 즉시 삭제합니다.
    void deleteResetToken(@Param("token") String token);

    List<SupplierDTO> findPendingSuppliers();
    void updateSupplierStatus(@Param("supplierId") String supplierId, @Param("status") String status);
    String getUserRole(String userId);
    void updateUserRole(@Param("userId") String userId, @Param("role") String role);
}