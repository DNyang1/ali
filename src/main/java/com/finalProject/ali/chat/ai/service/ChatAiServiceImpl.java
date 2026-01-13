package com.finalProject.ali.chat.ai.service;

import com.finalProject.ali.chat.ai.dto.AiDraftRequestDTO;
import com.finalProject.ali.chat.ai.dto.AiSuggestionDTO;
import com.finalProject.ali.chat.ai.dto.AiSuggestionsResponseDTO;
import com.finalProject.ali.chat.dao.ChatDAO;
import com.finalProject.ali.chat.service.ChatService;
import com.finalProject.ali.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatAiServiceImpl implements ChatAiService {

    private static final Set<String> ALLOWED_KEYS =
            Set.of("PRICE", "MOQ", "DELIVERY", "SAMPLE", "QUALITY", "PAYMENT", "CERT");

    private final ChatService chatService;
    private final ChatOpenAiClient chatOpenAiClient;
    private final ProductService productService;
    private final ChatDAO chatDAO;

    /* ===================== prompts ===================== */

    private static final String COMMON_PROMPT = """
너는 B2B 채팅에서 '현재 사용자'가 다음에 보낼 추천 버튼 문장 4개를 생성한다.

반드시 아래 형식으로만 출력한다(정확히 4줄).
다른 설명/번호/JSON/빈 줄/머리말 금지.

KEY|text
KEY|text
KEY|text
KEY|text

KEY 후보: PRICE / MOQ / DELIVERY / SAMPLE / QUALITY / PAYMENT / CERT

공통 규칙:
- text는 한글 존댓말, 15~40자, 버튼용으로 짧게
- 최근 대화 흐름을 반영해서 “다음 단계”가 되게 만들 것
- 4개는 서로 다른 KEY로 구성할 것(중복 금지)
- 이미 대화에서 답이 나온 주제(KEY)는 추천에서 제외할 것
- productId가 있으면 제품 관련 질문을 우선
""";

    private static final String BUYER_PROMPT = COMMON_PROMPT + """
role=BUYER: 판매자에게 묻는 질문 형태로만 생성한다.
(구매자 → 판매자에게 조건/정보 요청)

예시(형식만 참고):
PRICE|수량별 단가와 견적 가능 범위를 알려주실 수 있을까요?
MOQ|MOQ(최소 주문수량)은 어떻게 되나요?
DELIVERY|납기(리드타임)와 배송 방식이 어떻게 되나요?
SAMPLE|샘플 제공이 가능한지와 비용이 궁금합니다.
QUALITY|제품 사양/품질 기준을 알 수 있을까요?
PAYMENT|결제 조건(T/T 등) 가능 범위를 알려주세요.
CERT|필요한 인증(KC/CE 등)이 포함되어 있나요?
""";

    private static final String SELLER_PROMPT = COMMON_PROMPT + """
role=SELLER: 구매자에게 정보 요청/조건 확인 질문 형태로만 생성한다.
(판매자 → 구매자에게 요구사항/조건 확인)

예시(형식만 참고):
DELIVERY|희망 납기 일정과 출고 희망일이 있으신가요?
MOQ|예상 수량(MOQ 포함)을 알려주시면 견적에 도움이 됩니다.
PAYMENT|선호하시는 결제 조건(T/T 등)이 있으신가요?
QUALITY|원하시는 소재/사양 기준이 있으면 알려주세요.
CERT|필요하신 인증(KC/CE 등)이 있으신가요?
SAMPLE|샘플 필요 여부와 수령 지역을 알려주실 수 있나요?
PRICE|목표 단가나 예산 범위가 있으신가요?
""";

    private static final String NEUTRAL_PROMPT = COMMON_PROMPT + """
role=NEUTRAL: BUYER 톤(판매자에게 묻는 질문)으로 일반적인 다음 질문 4개를 생성한다.
""";

    private String selectPromptByRole(String role) {
        if ("SELLER".equalsIgnoreCase(role)) return SELLER_PROMPT;
        if ("BUYER".equalsIgnoreCase(role)) return BUYER_PROMPT;
        return NEUTRAL_PROMPT;
    }

    /* ===================== suggestions ===================== */

    @Override
    public AiSuggestionsResponseDTO getSuggestions(Long roomId, String myUserId, Long productId) {

        String context = safe(chatService.buildConversationContext(roomId));

        Long lastProductId = (roomId == null ? null : chatDAO.findLastProductIdInRoom(roomId));
        Long resolvedProductId = (productId != null ? productId : lastProductId);

        String role = resolveRole(resolvedProductId, myUserId);

        // 디버깅 로그(문제 생기면 이거로 바로 원인 찾음)
        try {
            String supplierId = (resolvedProductId == null ? null : productService.findSupplierIdByProductId(resolvedProductId));
            log.info("[AI] roomId={}, myUserId={}, reqProductId={}, lastProductId={}, resolvedProductId={}, supplierId={}, role={}",
                    roomId, myUserId, productId, lastProductId, resolvedProductId, supplierId, role);
        } catch (Exception e) {
            log.warn("[AI] debug log failed: {}", e.getMessage());
        }

        String instructions = selectPromptByRole(role);

        String userText = """
내 userId: %s
내 role: %s
productId: %s
최근 대화:
%s
""".formatted(
                safe(myUserId),
                safe(role),
                (resolvedProductId == null ? "null" : resolvedProductId),
                context
        );

        String raw = chatOpenAiClient.generateText(instructions, userText);

        List<AiSuggestionDTO> parsed = parseSuggestionsLines(raw, role);
        if (parsed.isEmpty()) parsed = defaultSuggestions(role);

        return new AiSuggestionsResponseDTO(parsed);
    }

    /* ===================== draft ===================== */

    @Override
    public String generateDraft(Long roomId, String myUserId, AiDraftRequestDTO req) {

        String purpose = safe(req.getPurpose());
        if (!StringUtils.hasText(purpose)) purpose = "PRICE";
        purpose = purpose.toUpperCase();

        Long productId = resolveProductId(roomId, req.getProductId());
        String context = safe(chatService.buildConversationContext(roomId));
        String role = resolveRole(productId, myUserId);

        String seedText = safe(req.getSeedText());
        String instructions = buildDraftInstructions(purpose, StringUtils.hasText(seedText));

        String userText = """
myUserId=%s
role=%s
productId=%s
purpose=%s
seedText=%s
recent_chat:
%s
""".formatted(
                safe(myUserId),
                safe(role),
                (productId == null ? "null" : productId),
                safe(purpose),
                (StringUtils.hasText(seedText) ? seedText : "null"),
                context
        );

        String draft = chatOpenAiClient.generateText(instructions, userText);
        if (!StringUtils.hasText(draft)) {
            if (StringUtils.hasText(seedText)) return seedText;
            return fallbackDraft(purpose);
        }

        return draft.trim();
    }

    /* ===================== role/product helpers ===================== */

    private String resolveRole(Long productId, String myUserId) {
        if (productId == null) return "NEUTRAL";

        String supplierId = productService.findSupplierIdByProductId(productId);
        if (supplierId == null) return "NEUTRAL";

        // ✅ s_ prefix 정규화 비교 (핵심!)
        String nSupplier = normalize(supplierId);
        String nMe = normalize(myUserId);

        if (!StringUtils.hasText(nMe) || !StringUtils.hasText(nSupplier)) return "NEUTRAL";
        return nSupplier.equals(nMe) ? "SELLER" : "BUYER";
    }

    @Override
    public Long resolveProductId(Long roomId, Long requestedProductId) {
        if (requestedProductId != null) return requestedProductId;
        if (roomId == null) return null;
        return chatDAO.findLastProductIdInRoom(roomId);
    }

    /* ===================== parsing + defaults ===================== */

    private List<AiSuggestionDTO> parseSuggestionsLines(String raw, String role) {

        if (!StringUtils.hasText(raw)) return List.of();

        List<AiSuggestionDTO> out = new ArrayList<>();
        String[] lines = raw.strip().split("\\R+");

        for (String line : lines) {
            if (out.size() >= 4) break;

            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            int bar = trimmed.indexOf('|');
            if (bar <= 0) continue;

            String key = trimmed.substring(0, bar).trim().toUpperCase();
            String text = trimmed.substring(bar + 1).trim();

            if (!ALLOWED_KEYS.contains(key)) continue;
            if (!StringUtils.hasText(text)) continue;

            // (선택) role과 너무 다른 톤이면 버림 — 섞임 방지에 도움됨
            if (!passesToneGuard(role, text)) continue;

            out.add(new AiSuggestionDTO(key, text));
        }

        // 4개 미만이면 기본값으로 채우기
        if (out.size() < 4) {
            for (AiSuggestionDTO d : defaultSuggestions(role)) {
                if (out.size() >= 4) break;
                boolean dup = out.stream().anyMatch(x -> x.getKey().equals(d.getKey()));
                if (!dup) out.add(d);
            }
        }
        return out;
    }

    private List<AiSuggestionDTO> defaultSuggestions(String role) {
        if ("SELLER".equalsIgnoreCase(role)) {
            return List.of(
                    new AiSuggestionDTO("DELIVERY", "희망 납기 일정과 출고 희망일이 있으신가요?"),
                    new AiSuggestionDTO("MOQ", "예상 수량(MOQ 포함)을 알려주시면 견적이 가능합니다."),
                    new AiSuggestionDTO("PAYMENT", "선호하시는 결제 조건(T/T 등)이 있으신가요?"),
                    new AiSuggestionDTO("QUALITY", "원하시는 사양/품질 기준이 있으면 공유 부탁드립니다.")
            );
        }
        // BUYER / NEUTRAL
        return List.of(
                new AiSuggestionDTO("PRICE", "수량 기준으로 단가/견적을 받을 수 있을까요?"),
                new AiSuggestionDTO("MOQ", "MOQ(최소 주문수량)이 어떻게 되나요?"),
                new AiSuggestionDTO("DELIVERY", "납기(리드타임)는 어느 정도 걸리나요?"),
                new AiSuggestionDTO("SAMPLE", "샘플 제공이 가능한가요?")
        );
    }

    /* ===================== draft instructions ===================== */

    private String buildDraftInstructions(String purpose, boolean hasSeedText) {

        String common = """
너는 B2B 커머스 채팅 메시지 초안을 작성하는 AI다.

중요 규칙:
- 절대 새로운 사실/숫자/날짜를 만들어내지 마라(리드타임, 단가, 출고일 등).
- 메타 발언(예: "다음과 같이", "아래는") 금지. 결과 문장만 출력.
- 한글 존댓말, 1~2문장.
""";

        if (hasSeedText) {
            return common + """
추가 규칙(필수):
- seedText는 사용자가 선택한 '보낼 질문 문장'이다.
- seedText의 의미를 유지하면서 더 자연스럽고 정중하게 다듬어라.
- 질문을 답변으로 바꾸지 마라.
- 반드시 질문 형태(물음표)로 끝내라.
- seedText에서 벗어나 새로운 주제를 추가하지 마라.
""";
        }

        return switch (purpose) {
            case "MOQ" -> common + """
MOQ(최소 주문수량)와 수량별 단가(가격 구간)를 자연스럽게 '질문'으로 작성하라.
반드시 질문으로 끝내라.
""";
            case "DELIVERY" -> common + """
납기(리드타임), 출고 가능 일정, 배송 방식(항공/해상)을 자연스럽게 '질문'으로 작성하라.
반드시 질문으로 끝내라.
""";
            case "SAMPLE" -> common + """
샘플 가능 여부, 샘플 비용/배송비, 샘플 리드타임을 '질문'으로 작성하라.
반드시 질문으로 끝내라.
""";
            case "PAYMENT" -> common + """
결제 조건(T/T 등), 선금/잔금 조건을 '질문'으로 작성하라.
반드시 질문으로 끝내라.
""";
            case "QUALITY" -> common + """
스펙/소재/검수 기준, 불량/클레임 정책을 '질문'으로 작성하라.
반드시 질문으로 끝내라.
""";
            case "CERT" -> common + """
인증서(KC/CE/RoHS 등) 보유 여부와 제공 가능 문서를 '질문'으로 작성하라.
반드시 질문으로 끝내라.
""";
            default -> common + """
단가/견적, 수량별 가격, 견적 유효기간을 '질문'으로 작성하라.
반드시 질문으로 끝내라.
""";
        };
    }

    private String fallbackDraft(String purpose) {
        return switch (purpose) {
            case "MOQ" -> "MOQ(최소 주문수량)와 수량별 단가가 어떻게 되는지 안내 부탁드립니다.";
            case "DELIVERY" -> "납기(리드타임)와 출고 가능 일정이 어떻게 되는지 문의드립니다.";
            case "SAMPLE" -> "샘플 제공 가능 여부와 비용/배송비 안내 부탁드립니다.";
            case "PAYMENT" -> "결제 조건과 선금/잔금 조건이 어떻게 되는지 확인 부탁드립니다.";
            case "QUALITY" -> "제품 스펙/소재와 검수 기준 관련 자료가 있을까요?";
            case "CERT" -> "관련 인증서(KC/CE/RoHS 등) 보유 여부와 제공 가능 문서가 있을까요?";
            default -> "수량 기준으로 단가/견적을 받을 수 있을까요?";
        };
    }

    /* ===================== tone guard (optional, but helpful) ===================== */

    private boolean passesToneGuard(String role, String text) {
        // 너무 빡세게 하면 좋은 문장도 걸러져서, 아주 약하게만 체크
        boolean looksBuyer = looksBuyerTone(text);
        boolean looksSeller = looksSellerTone(text);

        if ("SELLER".equalsIgnoreCase(role)) {
            // 판매자 role인데 구매자 톤이 강하면 제외
            return !looksBuyer || looksSeller;
        }
        if ("BUYER".equalsIgnoreCase(role)) {
            // 구매자 role인데 판매자 톤이 강하면 제외
            return !looksSeller || looksBuyer;
        }
        return true; // NEUTRAL은 통과
    }

    private boolean looksBuyerTone(String text) {
        // 구매자 → 판매자에게 요청할 때 흔한 표현
        return containsAny(text, "알려주실", "안내해주실", "가능할까요", "부탁드립니다", "궁금합니다");
    }

    private boolean looksSellerTone(String text) {
        // 판매자 → 구매자에게 요청할 때 흔한 표현
        return containsAny(text, "알려주시면", "공유 부탁", "말씀 부탁", "있으신가요", "원하시는", "필요하신");
    }

    private boolean containsAny(String text, String... needles) {
        if (!StringUtils.hasText(text)) return false;
        for (String n : needles) {
            if (text.contains(n)) return true;
        }
        return false;
    }

    /* ===================== misc ===================== */

    private String safe(String s) {
        return (s == null ? "" : s.trim());
    }

    private String normalize(String id) {
        if (id == null) return null;
        String t = id.trim();
        return t.startsWith("s_") ? t.substring(2) : t;
    }
}
