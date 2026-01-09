let stompClient = null;

// roomId -> { msgSub, readSub }
const roomSubs = new Map();

let currentRoomId = null;
let currentUserId = null;

// Draft(LocalStorage) Utils
const DRAFT_KEY_PREFIX = "chatDraftProductId:"; // roomId별 저장

function draftKey(roomId) {
    roomId = toNum(roomId);
    return roomId ? `${DRAFT_KEY_PREFIX}${roomId}` : null;
}

function setDraft(roomId, productId) {
    const key = draftKey(roomId);
    if (!key) return;

    if (!productId) {
        localStorage.removeItem(key);
        return;
    }
    localStorage.setItem(key, String(productId));
}

function getDraft(roomId) {
    const key = draftKey(roomId);
    if (!key) return null;
    return localStorage.getItem(key); // "117"
}

function clearDraft(roomId) {
    const key = draftKey(roomId);
    if (!key) return;
    localStorage.removeItem(key);
}

window.addEventListener("DOMContentLoaded", async () => {
    currentRoomId = toNum(document.getElementById("currentRoomId")?.value);
    currentUserId = (document.getElementById("currentUserId")?.value || "").trim() || null;

    console.log("[INIT]", { currentRoomId, currentUserId });

    // 전송 이벤트
    document.getElementById("sendBtn")?.addEventListener("click", sendMessage);
    document.getElementById("messageInput")?.addEventListener("keydown", (e) => {
        if (e.key === "Enter") sendMessage();
    });

    // 방 클릭
    bindRoomClicks();
    bindRoomSearch();

    // 드래프트 바인딩(새로고침 복원 포함)
    bindProductDraft();

    // WS 연결
    connect();

    // 최초 진입: 현재 방이 있으면 로딩 + 읽음 처리
    if (currentRoomId) {
        await loadMessages(currentRoomId);
        await markRead(currentRoomId);
        setBadge(currentRoomId, 0);
    }
});

function connect() {
    const socket = new SockJS("/ws");
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect(
        {},
        () => {
            console.log("[WS] CONNECTED");

            // 화면에 렌더된 모든 방을 전부 구독
            subscribeAllRoomsFromDom();
        },
        (err) => console.log("[WS] CONNECT ERROR", err)
    );
}

function subscribeAllRoomsFromDom() {
    document.querySelectorAll(".room-item").forEach((el) => {
        const rid = toNum(el.dataset.roomId);
        if (!rid) return;
        subscribeRoomTopics(rid);
    });
}

function subscribeRoomTopics(roomId) {
    roomId = toNum(roomId);
    if (!roomId) return;
    if (!stompClient?.connected) return;

    // 이미 구독한 방이면 스킵
    if (roomSubs.has(roomId)) return;

    const msgTopic = `/topic/rooms/${roomId}`;
    const readTopic = `/topic/rooms/${roomId}/read`;

    console.log("[WS] SUBSCRIBE", { msgTopic, readTopic });

    const msgSub = stompClient.subscribe(msgTopic, async (frame) => {
        let msg;
        try {
            msg = JSON.parse(frame.body);
        } catch {
            return;
        }
        if (!msg?.roomId) return;

        // 왼쪽 리스트: 마지막 메시지 + 맨 위로
        updateRoomLastMessage(msg.roomId, msg.message);

        // 내가 보고있는 방이면 append
        if (toNum(msg.roomId) === toNum(currentRoomId)) {
            appendMessage(msg);

            // 상대가 보낸 메시지면: 내가 보고 있으니 즉시 읽음 처리 + 배지 0
            if (msg.senderId && msg.senderId !== currentUserId) {
                await markRead(currentRoomId);
                setBadge(currentRoomId, 0);
            }
            return;
        }

        // 다른 방이면 배지 +1 (내가 보낸 건 제외)
        if (msg.senderId && msg.senderId !== currentUserId) {
            const now = getBadgeCount(msg.roomId);
            setBadge(msg.roomId, now + 1);
        }
    });

    const readSub = stompClient.subscribe(readTopic, (frame) => {
        let evt;
        try {
            evt = JSON.parse(frame.body);
        } catch {
            return;
        }

        // evt: { roomId, readerId, lastReadChatId }
        if (!evt?.roomId || !evt?.lastReadChatId) return;

        // 내가 읽은 이벤트는 무시 (내 말풍선에 '읽음'은 상대가 읽어야 뜸)
        if (evt.readerId === currentUserId) return;

        // 지금 보고 있는 방에서만 말풍선 읽음표시 반영
        if (toNum(evt.roomId) === toNum(currentRoomId)) {
            applyReadMarks(evt.lastReadChatId);
        }
    });

    roomSubs.set(roomId, { msgSub, readSub });
}

function bindRoomClicks() {
    document.querySelectorAll(".room-item").forEach((el) => {
        el.addEventListener("click", async () => {
            const rid = toNum(el.dataset.roomId);
            if (!rid) return;
            if (rid === currentRoomId) return;

            // active 토글
            document.querySelectorAll(".room-item").forEach((x) => x.classList.remove("active"));
            el.classList.add("active");

            // 방 바꾸면 드래프트는 제거(정책 유지)
            clearDraft(currentRoomId);      // 기존 방의 draft 제거
            draftProductId = null;
            const draftEl = document.getElementById("productDraft");
            if (draftEl) draftEl.style.display = "none";

            // 현재 방 변경
            currentRoomId = rid;
            const hidden = document.getElementById("currentRoomId");
            if (hidden) hidden.value = String(rid);

            const oppName = (el.dataset.opponentName || "").trim();
            setHeader(oppName || `Room #${rid}`, "");

            // 메시지 로딩
            await loadMessages(rid);

            // 읽음 처리 + 배지 0
            await markRead(rid);
            setBadge(rid, 0);

            // URL만 변경
            const url = new URL(location.href);
            url.pathname = "/chat/messages-user";
            url.searchParams.set("roomId", rid);
            url.searchParams.delete("productId"); // 혹시 남아있을까봐 제거
            history.replaceState(null, "", url.pathname + "?" + url.searchParams.toString());

            // 새 방의 드래프트 복원(있으면)
            bindProductDraft();
        });
    });
}

async function loadMessages(roomId) {
    const chatBody = document.getElementById("chatBody");
    if (!chatBody) return;

    chatBody.innerHTML = "";

    const res = await fetch(`/chat/api/rooms/${roomId}/messages`, {
        method: "GET",
        headers: { "Accept": "application/json" }
    });

    if (!res.ok) {
        chatBody.innerHTML = `<div style="padding:10px;">메시지 로드 실패 (${res.status})</div>`;
        return;
    }

    const data = await res.json();
    const list = data.messages || [];
    const opponentLastReadChatId = Number(data.opponentLastReadChatId || 0);

    if (!Array.isArray(list) || list.length === 0) {
        chatBody.innerHTML = `<div style="padding:10px; opacity:.7;">메시지가 없습니다.</div>`;
        return;
    }

    list.forEach(appendMessage);
    chatBody.scrollTop = chatBody.scrollHeight;

    // 새로고침/방이동 직후에도 읽음 표시 복구
    applyReadMarks(opponentLastReadChatId);
}

function sendMessage() {
    const input = document.getElementById("messageInput");
    const text = (input?.value || "").trim();

    // 텍스트도 없고, 드래프트도 없으면 전송 X
    if (!text && !draftProductId) return;
    if (!stompClient?.connected) return;
    if (!currentRoomId || !currentUserId) return;

    const payload = {
        roomId: currentRoomId,
        senderId: currentUserId,
        message: text || "",          // 텍스트 없이 상품만 보내도 OK
        productId: draftProductId
    };

    stompClient.send("/app/chat.send", {}, JSON.stringify(payload));

    // 입력 초기화
    input.value = "";
    input.focus();

    // 보내고 나면 드래프트 제거(정책)
    if (draftProductId) {
        draftProductId = null;

        // UI 숨김
        const draftEl = document.getElementById("productDraft");
        if (draftEl) draftEl.style.display = "none";

        // URL에서 productId 제거
        const u = new URL(location.href);
        u.searchParams.delete("productId");
        history.replaceState(null, "", u.pathname + (u.searchParams.toString() ? "?" + u.searchParams.toString() : ""));

        // localStorage에서도 제거
        clearDraft(currentRoomId);
    }
}

function appendMessage(m) {
    const chatBody = document.getElementById("chatBody");
    if (!chatBody) return;

    const isMe = m.senderId === currentUserId;
    const time = m.chatAt ? formatTime(m.chatAt) : "";
    const senderLabel = !isMe ? (m.senderName || m.senderId || "") : "";

    // wrapper: 정렬 담당
    const wrap = document.createElement("div");
    wrap.className = "msg-wrap" + (isMe ? " me" : "");

    // 이름(상대만)
    if (!isMe && senderLabel) {
        const nameEl = document.createElement("div");
        nameEl.className = "sender";
        nameEl.textContent = senderLabel;
        wrap.appendChild(nameEl);
    }

    // bubble: 말풍선(여기엔 me 붙이지 말 것!)
    const bubble = document.createElement("div");
    bubble.className = "msg";
    if (m.chatId != null) bubble.dataset.chatId = String(m.chatId);

    bubble.innerHTML = `
    <div class="text"></div>
    <div class="meta">
      <span class="time">${escapeHtml(time)}</span>
      <span class="read" style="display:none;">읽음</span>
    </div>
  `;

    // 상품 링크면 카드로, 아니면 텍스트
    const textEl = bubble.querySelector(".text");
    if (m.productId) {
        textEl.innerHTML = renderMessageContent(m);
        enhanceProductCard(bubble);
    } else {
        textEl.textContent = m.message || "";
    }

    wrap.appendChild(bubble);
    chatBody.appendChild(wrap);
    chatBody.scrollTop = chatBody.scrollHeight;
}

function renderMessageContent(m) {
    const text = (m.message || "").trim();
    const pid = m.productId ? String(m.productId) : null;

    if (pid) {
        const url = `/products/${encodeURIComponent(pid)}`;
        const title = text ? escapeHtml(text) : "상품 문의드립니다";

        return `
      <a class="product-card" data-product-id="${escapeHtml(pid)}"
         href="${url}" target="_blank" rel="noopener">
        <img class="pc-thumb" alt="" style="display:none;" />
        <div class="pc-body">
          <div class="pc-name">상품 #${escapeHtml(pid)} 보러가기</div>
          <div class="pc-title">${title}</div>
        </div>
      </a>
    `;
    }

    return escapeHtml(text);
}

function applyReadMarks(lastReadChatId) {
    const chatBody = document.getElementById("chatBody");
    if (!chatBody) return;

    const myMsgs = chatBody.querySelectorAll(`.msg-wrap.me .msg[data-chat-id]`);
    myMsgs.forEach((el) => {
        const chatId = toNum(el.dataset.chatId);
        if (chatId && chatId <= lastReadChatId) {
            const readEl = el.querySelector(".read");
            if (readEl) readEl.style.display = "inline";
        }
    });
}

async function markRead(roomId) {
    try {
        const res = await fetch(`/chat/api/rooms/${roomId}/read`, { method: "POST" });
        if (!res.ok) console.log("[READ] markRead failed", res.status);
    } catch (e) {
        console.log("[READ] markRead error", e);
    }
}

/* ======= 배지 ======= */
function getBadgeCount(roomId) {
    const badge = getBadgeEl(roomId);
    if (!badge) return 0;
    return toNum(badge.textContent) || 0;
}

function setBadge(roomId, count) {
    const roomEl = document.querySelector(`.room-item[data-room-id="${roomId}"]`);
    if (!roomEl) return;

    let badge = getBadgeEl(roomId);
    if (!badge) {
        const title = roomEl.querySelector(".room-title");
        if (!title) return;

        badge = document.createElement("span");
        badge.className = "badge";
        title.appendChild(badge);
    }

    const n = Math.max(0, toNum(count) || 0);
    badge.textContent = String(n);
    badge.style.display = n === 0 ? "none" : "";
}

function getBadgeEl(roomId) {
    const roomEl = document.querySelector(`.room-item[data-room-id="${roomId}"]`);
    if (!roomEl) return null;
    return roomEl.querySelector(".badge");
}

/* ======= 왼쪽 리스트 last message + 상단 이동 ======= */
function updateRoomLastMessage(roomId, lastMessage) {
    const roomList = document.getElementById("roomList");
    const el = document.querySelector(`.room-item[data-room-id="${roomId}"]`);
    if (!el) return;

    const sub = el.querySelector(".room-sub");
    if (sub) sub.textContent = lastMessage || "";

    if (roomList) roomList.prepend(el);
}

/* ======= UI ======= */
function setHeader(title, sub) {
    const t = document.querySelector(".chat-title");
    const s = document.querySelector(".chat-sub");
    if (t) t.textContent = title ?? "";
    if (s) s.textContent = sub ?? "";
}

/* ======= utils ======= */
function toNum(v) {
    const n = Number(v);
    return Number.isFinite(n) ? n : null;
}

function formatTime(v) {
    return String(v).slice(5, 16).replace("T", " ");
}

function escapeHtml(s) {
    return String(s)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function bindRoomSearch() {
    const input = document.querySelector(".sidebar-top .search");
    const list = document.getElementById("roomList");
    if (!input || !list) return;

    // room-item 노드들을 캐싱 (성능 + 검색 안정)
    const items = Array.from(list.querySelectorAll(".room-item")).map((el) => {
        const titleEl = el.querySelector(".room-title");
        const subEl = el.querySelector(".room-sub");

        // roomId도 검색 대상에 포함시키면 편함
        const roomId = (el.dataset.roomId || "").toString();

        const titleText = (titleEl?.innerText || "").trim();
        const subText = (subEl?.innerText || "").trim();

        // 검색용 텍스트(소문자)
        const haystack = `${roomId} ${titleText} ${subText}`.toLowerCase();

        return { el, haystack };
    });

    const applyFilter = () => {
        const q = (input.value || "").trim().toLowerCase();

        // 빈 값이면 전부 보이기
        if (!q) {
            items.forEach(({ el }) => (el.style.display = ""));
            return;
        }

        items.forEach(({ el, haystack }) => {
            el.style.display = haystack.includes(q) ? "" : "none";
        });
    };

    // 입력 이벤트에 연결
    input.addEventListener("input", applyFilter);

    // ESC 누르면 검색 초기화
    input.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            input.value = "";
            applyFilter();
            input.blur();
        }
    });
}

function getQueryParam(name) {
    return new URLSearchParams(location.search).get(name);
}

function setQueryParam(name, value) {
    const url = new URL(location.href);
    if (value === null || value === undefined || value === "") {
        url.searchParams.delete(name);
    } else {
        url.searchParams.set(name, String(value));
    }
    history.replaceState(null, "", url.pathname + "?" + url.searchParams.toString());
}

/** =========================
 *  Product Draft
 *  ========================= */
let draftProductId = null; // 현재 첨부된 상품 상태(전송에 사용)

function bindProductDraft() {
    const draftEl = document.getElementById("productDraft");
    const linkEl = document.getElementById("productDraftLink");
    const removeBtn = document.getElementById("productDraftRemove");
    const imgEl = draftEl?.querySelector(".product-draft__img img");

    if (!draftEl || !linkEl || !removeBtn) return;

    // roomId 기준으로 복원
    const url = new URL(location.href);
    const rid = toNum(url.searchParams.get("roomId")) || currentRoomId;

    // URL에서 productId 읽기(있으면 우선)
    const pidFromUrl = url.searchParams.get("productId");
    const pid = pidFromUrl || (rid ? getDraft(rid) : null);

    // 없으면 숨김 + 상태 초기화
    if (!pid) {
        draftEl.style.display = "none";
        draftProductId = null;
        return;
    }

    // 상태 세팅
    draftProductId = Number(pid) || null;

    // localStorage에 저장 (새로고침 복원용)
    if (rid) setDraft(rid, pid);

    // 링크 세팅
    linkEl.href = `/products/${encodeURIComponent(pid)}`;
    draftEl.style.display = "flex";

    hydrateDraftUI(pid);

    async function hydrateDraftUI(pid) {
        const draftEl = document.getElementById("productDraft");
        if (!draftEl) return;

        const titleEl = draftEl.querySelector(".product-draft__title");
        const imgEl = draftEl.querySelector(".product-draft__img img");
        const linkEl = document.getElementById("productDraftLink");

        try {
            const res = await fetch(`/api/products/${encodeURIComponent(pid)}/summary`);
            if (!res.ok) return;

            const s = await res.json();

            // 상품명
            if (titleEl && s?.productName) {
                titleEl.textContent = s.productName;
            }

            // 링크 텍스트
            if (linkEl) {
                linkEl.textContent = "상품으로 이동";
                linkEl.href = `/products/${pid}`;
            }

            // 썸네일
            if (imgEl && s?.thumbnailUrl) {
                imgEl.src = s.thumbnailUrl;
                imgEl.alt = s.productName || "상품 이미지";
                imgEl.style.display = "block";
                imgEl.onerror = () => imgEl.style.display = "none";
            }

        } catch (e) {
            console.log("[draft summary] fail", e);
        }
    }

    // URL에 productId가 있었다면 제거(깔끔)
    if (pidFromUrl) {
        url.searchParams.delete("productId");
        history.replaceState(null, "", url.pathname + "?" + url.searchParams.toString());
    }

    // X 버튼: 드래프트 숨기기 + 저장된 draft도 제거
    removeBtn.onclick = () => {
        draftEl.style.display = "none";
        draftProductId = null;

        // URL에서 productId 제거
        const u = new URL(location.href);
        u.searchParams.delete("productId");
        history.replaceState(null, "", u.pathname + (u.searchParams.toString() ? "?" + u.searchParams.toString() : ""));

        // localStorage에서도 제거
        if (rid) clearDraft(rid);
    };
}

const productCache = new Map(); // pid -> summary

async function enhanceProductCard(containerEl) {
    const card = containerEl.querySelector(".product-card[data-product-id]");
    if (!card) return;

    const pid = card.dataset.productId;
    if (!pid) return;

    // 캐시 있으면 바로 적용
    if (productCache.has(pid)) {
        applyProductSummary(card, productCache.get(pid));
        return;
    }

    try {
        const res = await fetch(`/api/products/${encodeURIComponent(pid)}/summary`);
        if (!res.ok) return;

        const summary = await res.json();
        productCache.set(pid, summary);
        applyProductSummary(card, summary);
    } catch (e) {
        console.log("[product summary] fail", e);
    }
}

function applyProductSummary(card, summary) {
    const nameEl = card.querySelector(".pc-name");
    const imgEl = card.querySelector(".pc-thumb");

    if (nameEl && summary?.productName) {
        nameEl.textContent = summary.productName;
    }

    if (imgEl && summary?.thumbnailUrl) {
        imgEl.src = summary.thumbnailUrl;
        imgEl.style.display = "";
        imgEl.onerror = () => { imgEl.style.display = "none"; };
    }
}
