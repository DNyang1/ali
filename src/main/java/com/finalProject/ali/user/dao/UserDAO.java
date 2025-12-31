package com.finalProject.ali.user.dao;

import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

}