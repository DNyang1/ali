let modalMode = null;

const RAW_SKUS = window.SKUS || [];
const PARSED_SKUS = Array.isArray(RAW_SKUS)
    ? RAW_SKUS
    : (typeof RAW_SKUS === 'string' ? JSON.parse(RAW_SKUS) : []);

const DEFAULT_PRICE_RULES_SAFE =
    window.DEFAULT_PRICE_RULES || [];

console.log('JS PARSED_SKUS 👉', PARSED_SKUS);
console.log('JS PRICE RULES 👉', DEFAULT_PRICE_RULES_SAFE);


function closeModal() {
    const modal = document.getElementById('orderModal');
    const overlay = document.getElementById('orderModalOverlay');
    if (!modal || !overlay) return;

    modal.style.display = 'none';
    overlay.style.display = 'none';
    document.body.style.overflow = 'auto';
}

document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('orderModal');
    const overlay = document.getElementById('orderModalOverlay');
    const openBtn = document.getElementById('openOrderModal');
    const openCartBtn = document.getElementById('openCartModal');
    const closeBtn = document.getElementById('closeOrderModal');

    const rules = normalizePriceRules(DEFAULT_PRICE_RULES);

    renderMainPriceTiers(rules);

    openBtn?.addEventListener('click', () => {
        modalMode = 'ORDER';
        openModal();
    });

    openCartBtn?.addEventListener('click', () => {
        modalMode = 'CART';
        openModal();
    });

    function openModal() {
        modal.style.display = 'block';
        overlay.style.display = 'block';
        document.body.style.overflow = 'hidden';

        renderPriceTiers(rules);
        renderSkuList(PARSED_SKUS);
        updateSummary();
        updateModalButtons();
    }

    function updateModalButtons() {
        document.getElementById('submitOrder').style.display =
            modalMode === 'ORDER' ? 'block' : 'none';
        document.getElementById('submitCart').style.display =
            modalMode === 'CART' ? 'block' : 'none';
    }

    closeBtn?.addEventListener('click', closeModal);
    overlay?.addEventListener('click', closeModal);

    document.getElementById('submitOrder')?.addEventListener('click', () => submit('ORDER'));
    document.getElementById('submitCart')?.addEventListener('click', () => submit('CART'));

    function submit(type) {
        const items = collectSkuItems();
        const totalQty = items.reduce((s, i) => s + i.quantity, 0);

        if (totalQty < MOQ) {
            alert(`최소 주문 수량은 ${MOQ}개입니다.`);
            return;
        }
        if (items.length === 0) {
            alert('수량을 선택하세요.');
            return;
        }

        const first = items[0];

        if (type === 'ORDER') {
            buyNow(first.skuId, first.quantity);
        } else {
            addToCart(first.skuId, first.quantity);
        }
    }
});

/* =========================
   SKU 리스트 렌더링
========================= */
function renderSkuList(skus) {
    const container = document.getElementById('skuList');
    if (!container) return;

    container.innerHTML = '';

    skus.forEach(sku => {
        const priceRules = sku.priceRules || [];
        const unitPrice = findUnitPriceByQty(priceRules, MOQ);

        const row = document.createElement('div');
        row.className = 'sku-row';
        row.dataset.skuId = sku.skuId;
        row.dataset.unitPrice = unitPrice;

        row.innerHTML = `
            <div class="sku-left">
                <div class="sku-id">${sku.skuId}</div>
                <div class="sku-stock">재고 ${sku.stockQuantity ?? 0}개</div>
            </div>
            <div class="sku-right">
                <span class="sku-price">₩${unitPrice.toLocaleString()}</span>
                <div class="qty-box">
                    <button class="qty-minus">-</button>
                    <span class="qty">0</span>
                    <button class="qty-plus">+</button>
                </div>
            </div>
        `;

        container.appendChild(row);
    });

    bindSkuQtyEvents();
}


/* =========================
   수량 +/- 이벤트
========================= */
function bindSkuQtyEvents() {
    document.querySelectorAll('.sku-row').forEach(row => {
        const minus = row.querySelector('.qty-minus');
        const plus = row.querySelector('.qty-plus');
        const qtyEl = row.querySelector('.qty');

        minus.addEventListener('click', () => {
            let q = Number(qtyEl.innerText);
            if (q > 0) q--;
            qtyEl.innerText = q;
            updateSummary();
        });

        plus.addEventListener('click', () => {
            let q = Number(qtyEl.innerText);
            const stock = Number(
                PARSED_SKUS.find(s => s.skuId === row.dataset.skuId)?.stockQuantity || 0
            );
            if (q < stock) q++;
            qtyEl.innerText = q;
            updateSummary();
        });
    });
}

/* =========================
   총 수량 / 금액
========================= */
function updateSummary() {
    let totalQty = 0;
    let totalPrice = 0;

    document.querySelectorAll('.sku-row').forEach(row => {
        const qty = Number(row.querySelector('.qty').innerText);
        const unitPrice = Number(row.dataset.unitPrice);

        totalQty += qty;
        totalPrice += qty * unitPrice;
    });

    document.getElementById('totalQty').innerText = totalQty;
    document.getElementById('totalPrice').innerText =
        totalPrice.toLocaleString() + '원';
}

/* =========================
   SKU 수집
========================= */
function collectSkuItems() {
    const items = [];

    document.querySelectorAll('.sku-row').forEach(row => {
        const qty = Number(row.querySelector('.qty').innerText);
        if (qty > 0) {
            items.push({
                skuId: row.dataset.skuId,
                quantity: qty
            });
        }
    });

    return items;
}

/* =========================
   가격 로직
========================= */
function normalizePriceRules(data) {
    if (Array.isArray(data)) return data;
    try { return JSON.parse(data); } catch { return []; }
}

function findUnitPriceByQty(priceRules, qty) {
    return [...priceRules]
        .sort((a, b) => b.minQty - a.minQty)
        .find(r => qty >= r.minQty && (r.maxQty == null || qty <= r.maxQty))
        ?.unitPrice || 0;
}

function renderPriceTiers(priceRules) {
    const container = document.getElementById('priceTiers');
    if (!container) return;

    container.innerHTML = '';
    priceRules.forEach(r => {
        const li = document.createElement('li');
        li.innerHTML = `
            <span>${r.maxQty ? `${r.minQty}~${r.maxQty}` : `${r.minQty}+`}개</span>
            <span>₩${Number(r.unitPrice).toLocaleString()}</span>
        `;
        container.appendChild(li);
    });
}

function renderMainPriceTiers(priceRules) {
    const container = document.getElementById('mainPriceTiers');
    if (!container) return;

    container.innerHTML = '';
    priceRules.forEach(r => {
        const div = document.createElement('div');
        div.innerHTML = `
            <div>${r.maxQty ? `${r.minQty}~${r.maxQty}` : `${r.minQty}+`}개</div>
            <div>₩${Number(r.unitPrice).toLocaleString()}</div>
        `;
        container.appendChild(div);
    });
}






/* ======================================================
   🔧 DEV ONLY : SKU 직접 선택 블록 (ORDER / CART 공통)
   ====================================================== */
(function () {
    // 필요하면 로컬에서만 활성화
    // if (location.hostname !== 'localhost') return;

    window.__DEV_FORCED_SKU_ID__ = null;

    document.addEventListener('DOMContentLoaded', () => {
        if (!Array.isArray(PARSED_SKUS) || PARSED_SKUS.length === 0) return;

        /* ===== DEV 컨테이너 ===== */
        const box = document.createElement('div');
        box.style.cssText = `
            margin-top:16px;
            padding:12px;
            border:1px dashed #999;
            border-radius:8px;
            background:#fafafa;
            font-size:13px;
        `;

        box.innerHTML = `
            <div style="margin-bottom:6px;font-weight:bold;">🔧 DEV TEST PANEL</div>

            <select id="devSkuSelect" style="width:100%;padding:6px;margin-bottom:6px;">
                <option value="">SKU 직접 선택</option>
            </select>

            <input id="devQtyInput" type="number" min="1" value="1"
                   style="width:100%;padding:6px;margin-bottom:8px;"/>

            <div style="display:flex;gap:6px;">
                <button id="devAddCart" style="flex:1;">장바구니 담기</button>
                <button id="devBuyNow" style="flex:1;">바로 주문</button>
            </div>
        `;

        /* ===== SKU 옵션 채우기 ===== */
        const select = box.querySelector('#devSkuSelect');
        PARSED_SKUS.forEach(sku => {
            const opt = document.createElement('option');
            opt.value = sku.skuId;
            opt.textContent = `${sku.skuId} | ${sku.optionValueIds?.join(', ')}`;
            select.appendChild(opt);
        });

        /* ===== 이벤트 ===== */
        select.addEventListener('change', e => {
            window.__DEV_FORCED_SKU_ID__ = e.target.value || null;
            console.log('🔧 [DEV] SKU =', window.__DEV_FORCED_SKU_ID__);
        });

        box.querySelector('#devAddCart').addEventListener('click', () => {
            const skuId = window.__DEV_FORCED_SKU_ID__;
            const qty = Number(box.querySelector('#devQtyInput').value || 1);

            if (!skuId) {
                alert('SKU를 선택하세요.');
                return;
            }

            addToCart(skuId, qty);
        });

        box.querySelector('#devBuyNow').addEventListener('click', () => {
            const skuId = window.__DEV_FORCED_SKU_ID__;
            const qty = Number(box.querySelector('#devQtyInput').value || 1);

            if (!skuId) {
                alert('SKU를 선택하세요.');
                return;
            }

            buyNow(skuId, qty);
        });

        /* ===== 페이지 하단에 추가 ===== */
        document.body.appendChild(box);
    });

    /* ===== resolveSkuId 후킹 (옵션 로직 무시) ===== */
    const originalResolveSkuId = window.resolveSkuId;
    window.resolveSkuId = function () {
        if (window.__DEV_FORCED_SKU_ID__) {
            return window.__DEV_FORCED_SKU_ID__;
        }
        return originalResolveSkuId();
    };
})();


function addToCart(skuId, quantity) {
    fetch('/api/cart/items', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            productId: PRODUCT_ID,
            skuId: skuId,
            quantity: quantity
        })
    })
        .then(res => {
            if (!res.ok) throw new Error('장바구니 추가 실패');
        })
        .then(() => {
            alert('장바구니에 담겼습니다.');
            closeModal();
        });
}
function buyNow(skuId, quantity) {
    fetch('/api/orders/preview/direct', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            productId: PRODUCT_ID,
            skuId: skuId,
            quantity: quantity
        })
    })
        .then(res => {
            if (!res.ok) throw new Error('바로 주문 미리보기 실패');
        })
        .then(() => {
            location.href = '/orders/checkout';
        });
}

// 송진영이 추가함
document.querySelector('.btn.chat')?.addEventListener('click', async (e) => {
    const productId = e.currentTarget.dataset.productId;

    const res = await fetch('/chat/api/rooms/start', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productId })
    });

    if (!res.ok) {
        alert('채팅방 생성 실패');
        return;
    }

    const data = await res.json();
    console.log('startChat response:', data);

    if (!data.roomId) {
        alert('roomId 없음');
        return;
    }

    location.href = `/chat/messages-user?roomId=${data.roomId}&productId=${productId}`;
});
