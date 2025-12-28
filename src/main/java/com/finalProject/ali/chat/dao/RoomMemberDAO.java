package com.finalProject.ali.chat.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoomMemberDAO {

    int insertMembers(@Param("roomId") Long roomId,
                      @Param("userIds") List<String> userIds);

    List<String> findMemberIds(@Param("roomId") Long roomId);

    int deleteMembersByRoomId(@Param("roomId") Long roomId);
}
