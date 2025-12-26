package com.finalProject.ali.users.DAO;

import com.finalProject.ali.users.DTO.UserDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDAO {
    void insertUser(UserDTO userDTO); // 회원가입
    UserDTO findByUserId(String userId);

}