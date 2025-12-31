let stompClient = null;

// roomId -> { msgSub, readSub }
const roomSubs = new Map();

let currentRoomId = null;
let currentUserId = null;

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

            // 현재 방 변경
            currentRoomId = rid;
            const hidden = document.getElementById("currentRoomId");
            if (hidden) hidden.value = String(rid);

            setHeader(`Room #${rid}`, "");

            // 메시지 로딩
            await loadMessages(rid);

            // 읽음 처리 + 배지 0
            await markRead(rid);
            setBadge(rid, 0);

            // URL만 변경
            history.replaceState(null, "", `/chat/messages?roomId=${rid}`);
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
    if (!text) return;

    if (!stompClient?.connected) return;
    if (!currentRoomId || !currentUserId) return;

    const payload = { roomId: currentRoomId, senderId: currentUserId, message: text };
    stompClient.send("/app/chat.send", {}, JSON.stringify(payload));

    input.value = "";
    input.focus();
}

function appendMessage(m) {
    const chatBody = document.getElementById("chatBody");
    if (!chatBody) return;

    const div = document.createElement("div");
    const isMe = m.senderId === currentUserId;
    div.className = "msg" + (isMe ? " me" : "");

    // 읽음표시를 하려면 chatId 필요
    if (m.chatId != null) div.dataset.chatId = String(m.chatId);

    const time = m.chatAt ? formatTime(m.chatAt) : "";

    div.innerHTML = `
    <div class="text"></div>
    <div class="meta">
      <span class="time">${escapeHtml(time)}</span>
      <span class="read" style="display:none;">읽음</span>
    </div>
  `;

    div.querySelector(".text").textContent = m.message || "";

    chatBody.appendChild(div);
    chatBody.scrollTop = chatBody.scrollHeight;
}

function applyReadMarks(lastReadChatId) {
    const chatBody = document.getElementById("chatBody");
    if (!chatBody) return;

    const myMsgs = chatBody.querySelectorAll(`.msg.me[data-chat-id]`);
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
