package com.finalProject.ali.mypage.user.dashboard.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDashboardDAO {

    int countReadyShipping(@Param("userId") String userId);

    int countShipping(@Param("userId") String userId);

    int countActiveInquiry(@Param("userId") String userId);
}
