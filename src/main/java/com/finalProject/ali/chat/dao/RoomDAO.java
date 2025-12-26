package com.finalProject.ali.chat.dao;

import com.finalProject.ali.chat.dto.RoomDTO;
import com.finalProject.ali.chat.dto.RoomListDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoomDAO {

    int insertRoom(RoomDTO room);

    RoomDTO findById(@Param("roomId") Long roomId);
    List<RoomListDTO> findMyRooms(@Param("userId") String userId);

}
