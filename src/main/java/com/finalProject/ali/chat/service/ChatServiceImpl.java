package com.finalProject.ali.chat.service;

import com.finalProject.ali.chat.dao.ChatDAO;
import com.finalProject.ali.chat.dao.RoomDAO;
import com.finalProject.ali.chat.dao.RoomMemberDAO;
import com.finalProject.ali.chat.dto.ChatDTO;
import com.finalProject.ali.chat.dto.RoomDTO;
import com.finalProject.ali.chat.dto.RoomListDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final RoomDAO roomDAO;
    private final RoomMemberDAO roomMemberDAO;
    private final ChatDAO chatDAO;

    @Override
    @Transactional
    public Long createRoom(List<String> userIds) {
        if (userIds == null || userIds.size() < 2) {
            throw new IllegalArgumentException("채팅방 멤버는 최소 2명 이상이어야 합니다.");
        }

        // room 생성
        RoomDTO roomDTO = new RoomDTO();
        int result = roomDAO.insertRoom(roomDTO);
        if (result != 1 || roomDTO.getRoomId() == null) {
            throw new IllegalStateException("채팅방 생성에 실패했습니다.");
        }

        // room_members에 멤버 등록 (N명 가능)
        int inserted = roomMemberDAO.insertMembers(roomDTO.getRoomId(), userIds);
        if (inserted != userIds.size()) {
            throw new IllegalStateException("채팅방 멤버 등록에 실패했습니다.");
        }

        return roomDTO.getRoomId();
    }

    @Override
    @Transactional
    public Long saveChat(Long roomId, String senderId, String message) {
        if (roomId == null) {
            throw new IllegalArgumentException("roomId는 필수입니다.");
        }
        if (senderId == null || senderId.isBlank()) {
            throw new IllegalArgumentException("senderId는 필수입니다.");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message는 비어있을 수 없습니다.");
        }

        // 방 멤버인지 검증 — 보안/무결성 위해 추천
        if (!isMember(roomId, senderId)) {
            throw new IllegalStateException("해당 유저는 이 채팅방 멤버가 아닙니다.");
        }

        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setRoomId(roomId);
        chatDTO.setSenderId(senderId);
        chatDTO.setMessage(message);

        int result = chatDAO.insertChat(chatDTO);
        if (result != 1 || chatDTO.getChatId() == null) {
            throw new IllegalStateException("채팅 저장에 실패했습니다.");
        }

        return chatDTO.getChatId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatDTO> getChatsByRoomId(Long roomId) {
        if (roomId == null) {
            throw new IllegalArgumentException("roomId는 필수입니다.");
        }
        return chatDAO.findChatsByRoomId(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMember(Long roomId, String userId) {
        if (roomId == null || userId == null) return false;
        List<String> members = roomMemberDAO.findMemberIds(roomId);
        return members != null && members.contains(userId);
    }

    @Override
    public List<RoomListDTO> getMyRooms(String userId) {
        return roomMemberDAO.getMyRooms(userId);
    }

    @Transactional
    public Long markAsRead(Long roomId, String userId) {
        Long lastChatId = chatDAO.findLastChatIdByRoomId(roomId);
        if (lastChatId != null && lastChatId > 0) {
            roomMemberDAO.updateLastReadChatId(roomId, userId, lastChatId);
            return lastChatId;
        }
        return 0L;
    }


    @Transactional(readOnly = true)
    public List<ChatDTO> getChatsForRoomWithReadStatus(Long roomId, String userId) {

        // 1) 채팅 목록
        List<ChatDTO> list = chatDAO.findChatsByRoomId(roomId);

        // 2) 상대방 last_read_chat_id (1:1)
        Long opponentLastRead = roomMemberDAO.findOpponentLastReadChatId(roomId, userId);
        if (opponentLastRead == null) opponentLastRead = 0L;

        // 3) readByOpponent 계산해서 넣기
        for (ChatDTO c : list) {
            boolean isMine = userId.equals(c.getSenderId());
            boolean readByOpponent = isMine && c.getChatId() != null && c.getChatId() <= opponentLastRead;
            c.setReadByOpponent(readByOpponent);
        }

        return list;
    }

}
