package com.finalProject.ali.mypage.user.order.dao;

import com.finalProject.ali.mypage.user.order.dto.UserRecentOrderResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserOrderDAO {

    List<UserRecentOrderResponse> findRecentOrdersByUserId(@Param("userId") String userId,
                                                           @Param("limit") int limit);
}
