package com.finalProject.ali.user.dao;

import com.finalProject.ali.user.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDAO {
    void insertUser(UserDTO userDTO); // 회원가입
    UserDTO findByUserId(String userId);

}