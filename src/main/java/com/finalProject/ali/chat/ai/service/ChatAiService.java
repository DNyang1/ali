package com.finalProject.ali.chat.ai.service;

import com.finalProject.ali.chat.ai.dto.AiDraftRequestDTO;
import com.finalProject.ali.chat.ai.dto.AiDraftRequestDTO;
import com.finalProject.ali.chat.dao.ChatProductSummaryDAO;
import com.finalProject.ali.chat.dto.ProductSummaryDTO;
import com.finalProject.ali.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatAiService {

    private final ChatService chatService;
    private final ChatProductSummaryDAO productSummaryDAO; // 상품명 가져오려고
    private final ChatOpenAiClient chatOpenAiClient;

    public String makeDraft(Long roomId, String userId, AiDraftRequestDTO req) {
        // 1) 방 멤버 검사
        if (!chatService.isMember(roomId, userId)) {
            throw new IllegalArgumentException("FORBIDDEN");
        }

        String purpose = (req.getPurpose() == null || req.getPurpose().isBlank())
                ? "PRICE"
                : req.getPurpose().trim().toUpperCase();

        Long productId = req.getProductId();
        String productName = null;

        if (productId != null) {
            ProductSummaryDTO ps = productSummaryDAO.findProductSummary(productId);
            if (ps != null) productName = ps.getProductName();
        }

        // 2) instructions + userText 구성
        String instructions =
                "너는 B2B 커머스 채팅에서 정중하고 간결한 메시지 초안을 만들어주는 도우미야. " +
                        "한국어로, 2~4문장으로, 바로 보내도 자연스럽게 작성해. " +
                        "가격/조건/납기/샘플 같은 거래 핵심을 명확히 묻고, 과한 이모지는 쓰지 마.";

        String userText = buildUserText(purpose, productId, productName);

        // 3) OpenAI 호출 (실패하면 템플릿 fallback)
        try {
            String out = chatOpenAiClient.generateDraft(instructions, userText);
            if (out != null && !out.isBlank()) return out.trim();
        } catch (Exception ignored) {
            // 로그는 필요하면 찍고, 데모 안정성을 위해 fallback
        }
        return fallbackTemplate(purpose, productName);
    }

    private String buildUserText(String purpose, Long productId, String productName) {
        String item = (productName != null && !productName.isBlank())
                ? productName
                : (productId != null ? ("상품#" + productId) : "해당 상품");

        return switch (purpose) {
            case "MOQ" -> item + " 최소 주문수량(MOQ)과 단가 기준(수량별 가격)이 어떻게 되나요?";
            case "LEAD_TIME" -> item + " 발주 후 생산/출고/배송까지 예상 리드타임이 어느 정도인지 안내 부탁드립니다.";
            case "SAMPLE" -> item + " 샘플 제공 가능 여부와 비용/배송기간을 알려주실 수 있을까요?";
            case "PRICE" -> item + " 현재 단가와 수량별 할인 조건, 결제/인코텀즈(가능하면) 안내 부탁드립니다.";
            default -> item + " 조건 확인을 위해 몇 가지 문의드립니다. MOQ/단가/납기 정보를 알려주세요.";
        };
    }

    private String fallbackTemplate(String purpose, String productName) {
        String item = (productName != null && !productName.isBlank()) ? productName : "해당 상품";

        return switch (purpose) {
            case "MOQ" -> "안녕하세요. " + item + " 문의드립니다.\n최소 주문수량(MOQ)과 수량별 단가(가격표)가 어떻게 되나요?";
            case "LEAD_TIME" -> "안녕하세요. " + item + " 납기 문의드립니다.\n발주 후 생산/출고까지 리드타임과 배송 예상 기간을 알려주세요.";
            case "SAMPLE" -> "안녕하세요. " + item + " 샘플 관련 문의드립니다.\n샘플 제공 가능 여부와 비용/배송 기간 안내 부탁드립니다.";
            case "PRICE" -> "안녕하세요. " + item + " 견적 문의드립니다.\n수량별 단가와 할인 조건, 결제 조건 안내 부탁드립니다.";
            default -> "안녕하세요. " + item + " 문의드립니다.\nMOQ/단가/납기 조건 안내 부탁드립니다.";
        };
    }
}
