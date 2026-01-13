package com.finalProject.ali.chat.service;

import com.finalProject.ali.chat.dto.ChatDTO;
import com.finalProject.ali.chat.dto.RoomListDTO;

import java.util.List;

public interface ChatService {

    // 채팅방 생성 + 멤버 등록을 하나의 작업으로 처리
    // @param userIds 방에 들어갈 유저 목록 (1:1이면 2명, 그룹이면 N명)
    // @return 생성된 roomId
    Long createRoom(List<String> userIds);

    // 채팅 메시지 저장
    Long saveChat(Long roomId, String senderId, String message, Long productId);

    // 방별 채팅 내역 조회
    List<ChatDTO> getChatsByRoomId(Long roomId);

    // 해당 유저가 방 멤버인지 확인
    boolean isMember(Long roomId, String userId);

    List<RoomListDTO> getMyRooms(String userId);

    Long markAsRead(Long roomId, String userId);

    List<ChatDTO> getChatsForRoomWithReadStatus(Long roomId, String userId);

    Long getOpponentLastReadChatId(Long roomId, String myUserId);

    Long findRoomIdByTwoMembers(String userA, String userB);

    String getUserName(String userId);

    List<ChatDTO> getRecentChatsForAi(Long roomId);

    String buildConversationContext(Long roomId);

}
