package com.finalProject.ali.chat.dao;

import com.finalProject.ali.chat.dto.ChatDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatDAO {

    Long saveChat(@Param("roomId") Long roomId,
                  @Param("senderId") String senderId,
                  @Param("message") String message,
                  @Param("productId") Long productId);

    List<ChatDTO> findChatsByRoomId(@Param("roomId") Long roomId);

    Long findLastChatIdByRoomId(@Param("roomId") Long roomId);

    Long findRoomIdByTwoMembers(@Param("userA") String userA, @Param("userB") String userB);

}
