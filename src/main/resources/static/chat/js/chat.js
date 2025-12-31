let stompClient = null;
let subscription = null;

let currentRoomId = null;
let currentUserId = null;

window.addEventListener("DOMContentLoaded", async () => {
    currentRoomId = Number(document.getElementById("currentRoomId")?.value) || null;
    currentUserId = document.getElementById("currentUserId")?.value || null;

    console.log("init", { currentRoomId, currentUserId });

    // WS 연결
    connect();

    // 전송 이벤트
    document.getElementById("sendBtn")?.addEventListener("click", sendMessage);
    document.getElementById("messageInput")?.addEventListener("keydown", (e) => {
        if (e.key === "Enter") sendMessage();
    });

    // 방 클릭 이벤트(비동기 전환)
    bindRoomClicks();

    // 최초 진입 시 roomId가 있으면: 메시지 로딩 + 읽음 처리
    if (currentRoomId) {
        await loadMessages(currentRoomId);
        await markRead(currentRoomId);         // 읽음 처리
        clearUnreadBadge(currentRoomId);       // 배지 0
    }
});

function connect() {
    const socket = new SockJS("/ws");
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect(
        {},
        () => {
            console.log("CONNECTED");
            if (currentRoomId) {
                subscribeRoom(currentRoomId);
            }
        },
        (err) => console.log("CONNECT ERROR", err)
    );
}

function bindRoomClicks() {
    document.querySelectorAll(".room-item").forEach((el) => {
        el.addEventListener("click", async () => {
            const rid = Number(el.dataset.roomId);
            if (!rid) return;

            // 같은 방이면 무시
            if (rid === currentRoomId) return;

            // UI active 토글
            document.querySelectorAll(".room-item").forEach((x) => x.classList.remove("active"));
            el.classList.add("active");

            // 현재 방 변경
            currentRoomId = rid;
            const hidden = document.getElementById("currentRoomId");
            if (hidden) hidden.value = String(rid);

            setHeader(`Room #${rid}`, "");

            // 메시지 로딩
            await loadMessages(rid);

            // 구독 변경
            if (stompClient?.connected) {
                subscribeRoom(rid);
            }

            // 읽음 처리 + 배지 제거
            await markRead(rid);
            clearUnreadBadge(rid);

            // URL만 변경
            history.replaceState(null, "", `/chat/messages?roomId=${rid}`);
        });
    });
}

function subscribeRoom(roomId) {
    roomId = Number(roomId);
    if (!roomId || !stompClient?.connected) return;

    // 기존 구독 해제
    if (subscription) {
        try { subscription.unsubscribe(); } catch (e) {}
        subscription = null;
    }

    console.log("SUBSCRIBE =>", `/topic/rooms/${roomId}`);

    subscription = stompClient.subscribe(`/topic/rooms/${roomId}`, async (frame) => {
        const msg = JSON.parse(frame.body);

        // 안전장치
        if (!msg?.roomId) return;

        // 왼쪽 리스트 마지막 메시지 갱신 + 맨 위로 이동
        updateRoomLastMessage(msg.roomId, msg.message);

        // 현재 보고 있는 방이면: 바로 append + 읽음 처리 + 배지 제거
        if (Number(msg.roomId) === Number(currentRoomId)) {
            appendMessage(msg);

            // 내가 보낸 메시지면 unread 올릴 필요 X
            // 상대가 보낸 메시지면 "내가 보고 있으니 읽음 처리"
            if (msg.senderId && msg.senderId !== currentUserId) {
                await markRead(currentRoomId);
                clearUnreadBadge(currentRoomId);
            }
            return;
        }

        // 다른 방 메시지면: 배지 +1 (내가 보낸 메시지는 증가 X)
        if (msg.senderId && msg.senderId !== currentUserId) {
            increaseUnreadBadge(msg.roomId, 1);
        }
    });
}

async function loadMessages(roomId) {
    const chatBody = document.getElementById("chatBody");
    if (!chatBody) return;

    chatBody.innerHTML = "";

    try {
        const res = await fetch(`/chat/api/rooms/${roomId}/messages`, {
            method: "GET",
            headers: { "Accept": "application/json" }
        });

        if (!res.ok) {
            chatBody.innerHTML = `<div style="padding:10px;">메시지를 불러오지 못했습니다. (${res.status})</div>`;
            return;
        }

        const list = await res.json();
        if (!Array.isArray(list) || list.length === 0) {
            chatBody.innerHTML = `<div style="padding:10px; opacity:.7;">메시지가 없습니다.</div>`;
            return;
        }

        list.forEach(appendMessage);
        chatBody.scrollTop = chatBody.scrollHeight;
    } catch (e) {
        console.log("loadMessages error", e);
        chatBody.innerHTML = `<div style="padding:10px;">서버 연결 오류</div>`;
    }
}

function sendMessage() {
    const input = document.getElementById("messageInput");
    const text = (input?.value || "").trim();
    if (!text) return;

    if (!stompClient?.connected) return;
    if (!currentRoomId || !currentUserId) return;

    const payload = {
        roomId: currentRoomId,
        senderId: currentUserId,
        message: text
    };

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

    const time = m.chatAt ? formatTime(m.chatAt) : "";
    const readMark = (isMe && m.readByOpponent) ? "읽음" : "";

    div.innerHTML = `
      <div class="text"></div>
      <div class="meta">
        <span class="time">${escapeHtml(time)}</span>
        ${readMark ? `<span class="read">${readMark}</span>` : ``}
      </div>
    `;

    div.querySelector(".text").textContent = m.message || "";

    chatBody.appendChild(div);
    chatBody.scrollTop = chatBody.scrollHeight;
}


function setHeader(title, sub) {
    const t = document.querySelector(".chat-title");
    const s = document.querySelector(".chat-sub");
    if (t) t.textContent = title ?? "";
    if (s) s.textContent = sub ?? "";
}

function updateRoomLastMessage(roomId, lastMessage) {
    const roomList = document.getElementById("roomList");
    const el = document.querySelector(`.room-item[data-room-id="${roomId}"]`);
    if (!el) return;

    const sub = el.querySelector(".room-sub");
    if (sub) sub.textContent = lastMessage || "";

    if (roomList) roomList.prepend(el);
}

// 읽음 처리 API
async function markRead(roomId) {
    if (!roomId) return;

    try {
        const res = await fetch(`/chat/api/rooms/${roomId}/read`, {
            method: "POST"
        });
        if (!res.ok) {
            console.log("markRead failed", res.status);
        }
    } catch (e) {
        console.log("markRead error", e);
    }
}

// unread badge 처리
function getBadgeEl(roomId) {
    const roomEl = document.querySelector(`.room-item[data-room-id="${roomId}"]`);
    if (!roomEl) return null;
    return roomEl.querySelector(".badge");
}

function clearUnreadBadge(roomId) {
    const roomEl = document.querySelector(`.room-item[data-room-id="${roomId}"]`);
    if (!roomEl) return;

    const badge = roomEl.querySelector(".badge");
    if (badge) badge.remove(); // 0이면 아예 제거
}

function increaseUnreadBadge(roomId, delta) {
    const roomEl = document.querySelector(`.room-item[data-room-id="${roomId}"]`);
    if (!roomEl) return;

    let badge = roomEl.querySelector(".badge");
    if (!badge) {
        // 없으면 새로 만들기
        const title = roomEl.querySelector(".room-title");
        if (!title) return;

        badge = document.createElement("span");
        badge.className = "badge";
        badge.textContent = "0";
        title.appendChild(badge);
    }

    const now = Number(badge.textContent || "0");
    badge.textContent = String(now + (delta || 0));
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
