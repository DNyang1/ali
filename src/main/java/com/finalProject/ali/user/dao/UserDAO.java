package com.finalProject.ali.user.dao;

import com.finalProject.ali.user.dto.SupplierDTO;
import com.finalProject.ali.user.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDAO {
    void insertUser(UserDTO userDTO); // 회원가입

    UserDTO findByUserId(String userId);

    // 업데이트
    void updateUser(UserDTO userDTO);
    void updateSupplier(SupplierDTO supplierDTO);

    // 구매자/판매자 찾기
    SupplierDTO findSupplierByUserId(String userId);
    void insertSupplier(SupplierDTO supplierDTO);
}