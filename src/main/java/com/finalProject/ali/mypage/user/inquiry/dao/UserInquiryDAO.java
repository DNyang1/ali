package com.finalProject.ali.mypage.user.inquiry.dao;

import com.finalProject.ali.mypage.user.inquiry.dto.UserRecentInquiryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserInquiryDAO {

    List<UserRecentInquiryResponse> findRecentByUserId(@Param("userId") String userId,
                                                       @Param("limit") int limit);
}
