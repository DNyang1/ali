package com.finalProject.ali.chat.controller;

import com.finalProject.ali.chat.dto.*;
import com.finalProject.ali.chat.service.ChatService;
import com.finalProject.ali.product.dto.ProductDTO;
import com.finalProject.ali.product.service.ProductService;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/api")
public class ChatRestController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ProductService productService;

    // 내 방 리스트
    @GetMapping("/rooms")
    public List<RoomListDTO> myRooms(HttpSession session) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return chatService.getMyRooms(loginUser.getUserId());
    }

    // 특정 방 메시지 조회
    @GetMapping("/rooms/{roomId}/messages")
    public ChatMessagesResponseDTO messages(@PathVariable Long roomId, HttpSession session) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String userId = loginUser.getUserId();
        if (!chatService.isMember(roomId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        List<ChatDTO> messages = chatService.getChatsByRoomId(roomId);

        // 상대가 마지막으로 읽은 chat_id
        Long opponentLastReadChatId = chatService.getOpponentLastReadChatId(roomId, userId);

        return new ChatMessagesResponseDTO(messages, opponentLastReadChatId);
    }

    // 읽음 처리
    @PostMapping("/rooms/{roomId}/read")
    public void markAsRead(@PathVariable Long roomId, HttpSession session) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String userId = loginUser.getUserId();
        if (!chatService.isMember(roomId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Long lastReadChatId = chatService.markAsRead(roomId, userId); // 리턴하도록 바꿈

        // 읽음 이벤트 브로드캐스트
        ReadEventDTO evt = new ReadEventDTO();
        evt.setRoomId(roomId);
        evt.setReaderId(userId);
        evt.setLastReadChatId(lastReadChatId);

        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/read", evt);
    }

    private String normalizeUserId(String userId) {
        if (userId == null) return null;
        if (userId.startsWith("s_")) {
            return userId.substring(2);
        }
        return userId;
    }

    // 상품 상세 → 채팅 시작
    @PostMapping("/rooms/start")
    public Map<String, Long> startChat(@RequestBody StartChatRequest req, HttpSession session) {
        UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
        if (loginUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Long productId = req.getProductId();
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId is required");
        }

        ProductDTO product = productService.productDetail(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다.");
        }

        // 채팅용 ID 정규화
        String buyerId = normalizeUserId(loginUser.getUserId());
        String sellerId = normalizeUserId(product.getSupplierId());

        System.out.println("buyerId(normalized) = " + buyerId);
        System.out.println("sellerId(normalized) = " + sellerId);

        // 자기 자신과 채팅 방지
        if (buyerId.equals(sellerId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "자기 자신과는 채팅할 수 없습니다."
            );
        }

        // 기존 방 재사용
        Long roomId = chatService.findRoomIdByTwoMembers(buyerId, sellerId);
        if (roomId == null) {
            roomId = chatService.createRoom(List.of(buyerId, sellerId));
        }

        return Map.of(
                "roomId", roomId,
                "productId", productId
        );
    }

}
