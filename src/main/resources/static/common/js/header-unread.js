(function () {
    // ✅ 이 로그가 안 찍히면: 아예 script가 페이지에 포함/실행이 안 된 것
    console.log("[header] script tag executed");

    // 중복 연결 방지
    if (window.__HEADER_UNREAD_WS_CONNECTED__) return;
    window.__HEADER_UNREAD_WS_CONNECTED__ = true;

    function normalizeUserId(userId) {
        if (!userId) return "";
        userId = String(userId).trim();
        return userId.startsWith("s_") ? userId.substring(2) : userId;
    }

    function updateHeaderUnread(total) {
        const el = document.getElementById("headerUnreadCount");
        if (!el) return;
        const n = Number(total || 0);
        el.textContent = String(Number.isFinite(n) ? n : 0);
    }

    document.addEventListener("DOMContentLoaded", () => {
        const headerEl = document.querySelector("header[data-user-id]");
        const rawUserId = headerEl?.getAttribute("data-user-id") || "";
        const userId = normalizeUserId(rawUserId);

        console.log("[header] DOM ready", {
            rawUserId,
            userId,
            SockJS: typeof SockJS,
            Stomp: typeof Stomp
        });

        if (!userId) return;

        // 1) 최초 REST 로딩
        fetch("/chat/api/unread-total", {headers: {"Accept": "application/json"}})
            .then(r => r.ok ? r.json() : null)
            .then(data => {
                console.log("[header] unread-total REST =", data);
                if (!data) return;
                updateHeaderUnread(data.total);
            })
            .catch(err => console.log("[header] REST error", err));

        // 2) WS 구독
        if (typeof SockJS === "undefined" || typeof Stomp === "undefined") {
            console.log("[header] SockJS/Stomp not loaded -> skip WS");
            return;
        }

        const socket = new SockJS("/ws");
        const client = Stomp.over(socket);
        client.debug = null;

        client.connect({}, () => {
            console.log("[header] WS connected, subscribe to", `/topic/users/${userId}/unread-total`);

            client.subscribe(`/topic/users/${userId}/unread-total`, (frame) => {
                console.log("[header] WS frame =", frame.body);
                try {
                    const data = JSON.parse(frame.body || "{}");
                    updateHeaderUnread(data.total);
                } catch (e) {
                    console.log("[header] WS parse error", e);
                }
            });
        }, (err) => {
            console.log("[header] WS connect error", err);
        });
    });
})();

