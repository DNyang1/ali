package com.finalProject.ali.chat.dao;

import com.finalProject.ali.chat.dto.RoomListDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoomMemberDAO {

    List<RoomListDTO> getMyRooms(@Param("userId") String userId);

    int insertMembers(@Param("roomId") Long roomId, @Param("userIds") List<String> userIds);

    List<String> findMemberIds(@Param("roomId") Long roomId);

    int deleteMembersByRoomId(@Param("roomId") Long roomId);

    void updateLastReadChatId(@Param("roomId") Long roomId, @Param("userId") String userId, @Param("chatId") Long chatId);

    Long findOpponentLastReadChatId(@Param("roomId") Long roomId, @Param("userId") String userId);

    String findOpponentId(@Param("roomId") Long roomId, @Param("myUserId") String myUserId);

}
