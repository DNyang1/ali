package com.finalProject.ali.chat.dao;

import com.finalProject.ali.chat.dto.ChatDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatDAO {

    int insertChat(ChatDTO chat);

    List<ChatDTO> findChatsByRoomId(@Param("roomId") Long roomId);
}
