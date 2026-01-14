package com.finalProject.ali.chat.service;

import com.finalProject.ali.chat.dao.ChatDAO;
import com.finalProject.ali.chat.dao.RoomDAO;
import com.finalProject.ali.chat.dao.RoomMemberDAO;
import com.finalProject.ali.chat.dto.ChatDTO;
import com.finalProject.ali.chat.dto.RoomDTO;
import com.finalProject.ali.chat.dto.RoomListDTO;
import com.finalProject.ali.user.dao.UserDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final RoomDAO roomDAO;
    private final RoomMemberDAO roomMemberDAO;
    private final ChatDAO chatDAO;
    private final UserDAO userDAO;

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
    public Long saveChat(Long roomId, String senderId, String message, Long productId) {

        if (roomId == null) {
            throw new IllegalArgumentException("roomId는 필수입니다.");
        }
        if (senderId == null || senderId.isBlank()) {
            throw new IllegalArgumentException("senderId는 필수입니다.");
        }
        if ((message == null || message.isBlank()) && productId == null) {
            throw new IllegalArgumentException("message 또는 productId 중 하나는 필요합니다.");
        }

        if (!isMember(roomId, senderId)) {
            throw new IllegalStateException("해당 유저는 이 채팅방 멤버가 아닙니다.");
        }

        String safeMessage = (message == null ? "" : message);
        Long chatId = chatDAO.saveChat(roomId, senderId, safeMessage, productId);

        if (chatId == null) {
            throw new IllegalStateException("채팅 저장에 실패했습니다.");
        }

        return chatId;
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

    @Override
    public Long getOpponentLastReadChatId(Long roomId, String myUserId) {
        Long v = roomMemberDAO.findOpponentLastReadChatId(roomId, myUserId);
        return (v == null ? 0L : v);
    }

    @Override
    public Long findRoomIdByTwoMembers(String userA, String userB) {
        return chatDAO.findRoomIdByTwoMembers(userA, userB);
    }

    @Override
    public String getUserName(String userId) {
        if (userId == null || userId.isBlank()) return null;

        String normalized = userId.startsWith("s_") ? userId.substring(2) : userId;
        return userDAO.findNameByUserId(normalized);
    }

    @Override
    public List<ChatDTO> getRecentChatsForAi(Long roomId) {
        return chatDAO.findRecentChatsByRoomId(roomId, 10);
    }

    @Override
    public String buildConversationContext(Long roomId) {
        List<ChatDTO> chats = getRecentChatsForAi(roomId);
        Collections.reverse(chats);

        return chats.stream()
                .map(c -> {
                    String name = (c.getSenderName() != null ? c.getSenderName() : c.getSenderId());
                    String msg  = (c.getMessage() != null ? c.getMessage() : "");
                    if (c.getProductId() != null) {
                        msg = msg.isBlank()
                                ? ("[상품링크 productId=" + c.getProductId() + "]")
                                : (msg + " [productId=" + c.getProductId() + "]");
                    }
                    return name + ": " + msg;
                })
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadTotal(String userId) {
        if (userId == null || userId.isBlank()) return 0L;

        String normalized = userId.startsWith("s_") ? userId.substring(2) : userId;

        List<RoomListDTO> rooms = roomMemberDAO.getMyRooms(normalized);
        if (rooms == null) return 0L;

        long total = 0L;
        for (RoomListDTO r : rooms) {
            if (r.getUnreadCount() != null) total += r.getUnreadCount();
        }
        return total;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getMemberIds(Long roomId) {
        return roomMemberDAO.findMemberIds(roomId);
    }

}
