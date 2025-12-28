let stompClient = null;
let subscription = null;

let currentRoomId = null;
let currentUserId = null;

window.addEventListener("DOMContentLoaded", () => {
    currentRoomId = Number(document.getElementById("currentRoomId")?.value);
    currentUserId = document.getElementById("currentUserId")?.value;

    console.log("init", { currentRoomId, currentUserId });

    // 1) WS 연결
    connect();

    // 2) 전송 이벤트
    document.getElementById("sendBtn")?.addEventListener("click", sendMessage);
    document.getElementById("messageInput")?.addEventListener("keydown", (e) => {
        if (e.key === "Enter") sendMessage();
    });

    // 3) 방 클릭 이벤트(비동기 전환)
    bindRoomClicks();
});

function connect() {
    const socket = new SockJS("/ws");
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect(
        {},
        async () => {
            console.log("CONNECTED");

            // 처음 진입 시 currentRoomId가 있으면 구독 + 메시지 로딩
            if (currentRoomId) {
                subscribeRoom(currentRoomId);
                await loadMessages(currentRoomId);
            }
        },
        (err) => {
            console.log("CONNECT ERROR", err);
        }
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

            // 헤더(임시)
            setHeader(`Room #${rid}`, "");

            // 메시지 비우고 새로 로딩
            await loadMessages(rid);

            // 구독 변경
            if (stompClient?.connected) {
                subscribeRoom(rid);
            }

            // URL만 바꾸고(새로고침 X) 싶으면
            history.replaceState(null, "", `/chat/messages?roomId=${rid}`);
        });
    });
}

function subscribeRoom(roomId) {
    roomId = Number(roomId);
    if (!roomId) {
        console.log("no roomId to subscribe");
        return;
    }
    if (!stompClient || !stompClient.connected) {
        console.log("subscribe skipped: not connected");
        return;
    }

    // 기존 구독 해제
    if (subscription) {
        try {
            subscription.unsubscribe();
        } catch (e) {}
        subscription = null;
    }

    console.log("SUBSCRIBE =>", `/topic/rooms/${roomId}`);

    subscription = stompClient.subscribe(`/topic/rooms/${roomId}`, (frame) => {
        console.log("RECV =>", frame.body);
        const msg = JSON.parse(frame.body);

        // 같은 방 메시지만 붙이기(안전장치)
        if (Number(msg.roomId) !== Number(currentRoomId)) return;

        appendMessage(msg);

        // 왼쪽 리스트 마지막 메시지 갱신(있으면)
        updateRoomLastMessage(msg.roomId, msg.message);
    });
}

async function loadMessages(roomId) {
    const chatBody = document.getElementById("chatBody");
    if (!chatBody) return;

    // 초기화
    chatBody.innerHTML = "";

    try {
        const res = await fetch(`/chat/api/rooms/${roomId}/messages`, {
            method: "GET",
            headers: { "Accept": "application/json" }
        });

        if (!res.ok) {
            console.log("loadMessages failed", res.status);
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

    console.log("SEND try", { roomId: currentRoomId, senderId: currentUserId, text });
    console.log("stomp connected?", stompClient?.connected);

    if (!stompClient || !stompClient.connected) {
        console.log("NOT CONNECTED");
        return;
    }
    if (!currentRoomId || !currentUserId) {
        console.log("Missing roomId/userId", { currentRoomId, currentUserId });
        return;
    }

    const payload = {
        roomId: currentRoomId,
        senderId: currentUserId,
        message: text
    };

    stompClient.send("/app/chat.send", {}, JSON.stringify(payload));
    console.log("SEND done");

    input.value = "";
    input.focus();
}

function appendMessage(m) {
    const chatBody = document.getElementById("chatBody");
    if (!chatBody) return;

    const div = document.createElement("div");
    div.className = "msg" + (m.senderId === currentUserId ? " me" : "");

    const time = m.chatAt ? formatTime(m.chatAt) : "";

    div.innerHTML = `
    <div class="text"></div>
    <div class="meta">
      <span>${escapeHtml(m.senderId || "")}</span> · <span>${escapeHtml(time)}</span>
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

    // 1) 마지막 메시지 텍스트 갱신
    const sub = el.querySelector(".room-sub");
    if (sub) sub.textContent = lastMessage || "";

    // 2) 최신 방을 리스트 맨 위로 이동
    // (active 방이면 active 유지됨)
    if (roomList) {
        roomList.prepend(el);
    }
}


function formatTime(v) {
    // v가 "2025-12-26T15:32:23.123" 같은 형태면 12-26 15:32 로
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
