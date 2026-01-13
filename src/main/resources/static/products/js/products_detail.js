let modalMode = null;
let isTimeSaleActive = true;

const RAW_SKUS = window.SKUS || [];
const PARSED_SKUS = Array.isArray(RAW_SKUS)
    ? RAW_SKUS
    : (typeof RAW_SKUS === 'string' ? JSON.parse(RAW_SKUS) : []);

const DEFAULT_PRICE_RULES_SAFE =
    window.DEFAULT_PRICE_RULES || [];

console.log('JS PARSED_SKUS 👉', PARSED_SKUS);
console.log('JS PRICE RULES 👉', DEFAULT_PRICE_RULES_SAFE);

function getDiscountedUnitPrice(unitPrice) {
    if (!isTimeSaleActive) return unitPrice;

    const box = document.querySelector('.time-sale-box');
    if (!box) return unitPrice;

    const type = box.dataset.type;
    const value = Number(box.dataset.value);

    if (type === 'RATE') {
        return Math.floor(unitPrice * (100 - value) / 100);
    }

    if (type === 'AMOUNT') {
        return Math.max(0, unitPrice - value);
    }

    return unitPrice;
}


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

    const rules = getRepresentativePriceRules(PARSED_SKUS);

    renderMainPriceTiers(rules);
    renderAllSkuInfo();

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

function getRepresentativePriceRules(skus) {
    if (!Array.isArray(skus) || skus.length === 0) return [];

    const baseSku = skus[0];

    return normalizePriceRules(baseSku.priceRules || []);
}


function renderSkuList(skus) {
    const container = document.getElementById('skuList');
    if (!container) return;

    container.innerHTML = '';

    skus.forEach(sku => {
        const unitPrice = findUnitPriceByQty(sku.priceRules || [], MOQ);
        const discounted = getDiscountedUnitPrice(unitPrice);
        const optionText = sku.optionSummary?.trim() || '옵션 없음';

        const row = document.createElement('div');
        row.className = 'sku-row';
        row.dataset.skuId = sku.skuId;
        row.dataset.unitPrice = unitPrice;
        row.dataset.discountedUnitPrice = discounted;

        row.innerHTML = `
            <div class="sku-left">
                <div class="sku-id">${sku.skuId}</div>
                <div class="sku-options">${optionText}</div>
                <div class="sku-stock">재고 ${sku.stockQuantity ?? 0}개</div>
            </div>

            <div class="sku-right">
                <div class="sku-price"></div>

                <div class="qty-box">
                    <button class="qty-minus">-</button>
                    <input class="qty-input" type="number" min="0" value="0">
                    <button class="qty-plus">+</button>
                </div>
            </div>
        `;

        const priceEl = row.querySelector('.sku-price');
        priceEl.innerText =
            discounted !== unitPrice
                ? `₩${discounted.toLocaleString()} (할인)`
                : `₩${unitPrice.toLocaleString()}`;

        container.appendChild(row);
    });

    bindSkuQtyEvents();
}



function renderAllSkuInfo() {
    const container = document.getElementById('mainSkuList');
    if (!container) return;
    if (!Array.isArray(PARSED_SKUS) || PARSED_SKUS.length === 0) return;

    container.innerHTML = PARSED_SKUS
        .map(sku => {
            const option =
                sku.optionSummary && sku.optionSummary.trim() !== ''
                    ? sku.optionSummary
                    : '옵션 없음';

            return `
                <div class="sku-all-item">
                    <span class="sku-all-id">${sku.skuId}</span>
                    <span class="sku-all-option">${option}</span>
                </div>
            `;
        })
        .join('');
}


function resolveUnitPriceByQty(skuId, qty) {
    const sku = PARSED_SKUS.find(s => s.skuId === skuId);
    if (!sku || !Array.isArray(sku.priceRules)) return 0;

    return [...sku.priceRules]
        .sort((a, b) => b.minQty - a.minQty) // 큰 구간부터
        .find(r =>
            qty >= r.minQty &&
            (r.maxQty == null || qty <= r.maxQty)
        )?.unitPrice || 0;
}

function bindSkuQtyEvents() {
    document.querySelectorAll('.sku-row').forEach(row => {
        const minus = row.querySelector('.qty-minus');
        const plus = row.querySelector('.qty-plus');
        const input = row.querySelector('.qty-input');
        const priceEl = row.querySelector('.sku-price');

        const skuId = row.dataset.skuId;
        const stock =
            PARSED_SKUS.find(s => s.skuId === skuId)?.stockQuantity ?? 0;

        function sync(qty) {
            if (qty < 0) qty = 0;
            if (qty > stock) qty = stock;

            input.value = qty;

            const unitPrice = resolveUnitPriceByQty(skuId, qty);
            const discounted = getDiscountedUnitPrice(unitPrice);

            row.dataset.unitPrice = unitPrice;
            row.dataset.discountedUnitPrice = discounted;

            priceEl.innerText =
                discounted !== unitPrice
                    ? `₩${discounted.toLocaleString()} (할인)`
                    : `₩${unitPrice.toLocaleString()}`;

            updateSummary();
        }

        minus.onclick = () => {
            sync(Number(input.value) - 1);
        };

        plus.onclick = () => {
            sync(Number(input.value) + 1);
        };

        input.oninput = () => {
            sync(Number(input.value));
        };
    });
}



function updateSummary() {
    let totalQty = 0;
    let totalPrice = 0;

    document.querySelectorAll('.sku-row').forEach(row => {
        const qty = Number(row.querySelector('.qty-input')?.value || 0);
        const unitPrice =
            Number(row.dataset.discountedUnitPrice || row.dataset.unitPrice);

        totalQty += qty;
        totalPrice += qty * unitPrice;
    });

    document.getElementById('totalQty').innerText = totalQty;
    document.getElementById('totalPrice').innerText =
        totalPrice.toLocaleString() + '원';
}

function collectSkuItems() {
    const items = [];

    document.querySelectorAll('.sku-row').forEach(row => {
        const qty = Number(row.querySelector('.qty-input')?.value || 0);

        if (qty > 0) {
            items.push({
                skuId: row.dataset.skuId,
                quantity: qty
            });
        }
    });

    return items;
}


function normalizePriceRules(rules) {
    if (!Array.isArray(rules)) {
        console.error("PRICE RULES IS NOT ARRAY", rules);
        return [];
    }

    const map = new Map();

    rules.forEach(r => {
        const minQty = r.minQty ?? 1;
        const maxQty = r.maxQty ?? null;
        const key = `${minQty}-${maxQty}`;

        if (!map.has(key)) {
            map.set(key, r.unitPrice);
        }
    });

    return [...map.entries()].map(([key, unitPrice]) => {
        const [minQty, maxQty] = key.split("-").map(v =>
            v === 'null' ? null : Number(v)
        );

        return {
            minQty,
            maxQty,
            unitPrice
        };
    });
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
        const original = Number(r.unitPrice);
        const discounted = getDiscountedUnitPrice(original);

        const li = document.createElement('li');

        li.innerHTML = `
            <span>
                ${r.maxQty ? `${r.minQty}~${r.maxQty}` : `${r.minQty}+`}개
            </span>
            <span>
                ₩${discounted.toLocaleString()}
            </span>
        `;

        container.appendChild(li);
    });
}

function renderMainPriceTiers(priceRules) {
    const container = document.getElementById('mainPriceTiers');
    if (!container) return;

    container.innerHTML = '';

    priceRules.forEach(r => {
        const original = Number(r.unitPrice);
        const discounted = getDiscountedUnitPrice(original);

        const div = document.createElement('div');
        div.className = 'main-price-tier';

        div.innerHTML = `
            <div class="tier-qty">
                ${r.maxQty ? `${r.minQty}~${r.maxQty}` : `${r.minQty}+`}개
            </div>

            ${
            discounted !== original
                ? `
                        <div class="tier-price original">
                            ₩${original.toLocaleString()}
                        </div>
                        <div class="tier-price discounted">
                            ₩${discounted.toLocaleString()}
                        </div>
                      `
                : `
                        <div class="tier-price">
                            ₩${original.toLocaleString()}
                        </div>
                      `
        }
        `;

        container.appendChild(div);
    });
}



(function () {
    const el = document.querySelector('.time-sale-countdown');
    if (!el) return;

    const endAt = new Date(el.dataset.endAt).getTime();

    const timer = setInterval(() => {
        const now = Date.now();
        const diff = endAt - now;

        if (diff <= 0) {
            el.innerText = '종료됨';
            clearInterval(timer);

            isTimeSaleActive = false;

            restoreOriginalPrices();
            return;
        }

        const day = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hour = Math.floor((diff / (1000 * 60 * 60)) % 24);
        const min = Math.floor((diff / (1000 * 60)) % 60);

        let text = '종료까지 ';
        if (day > 0) text += `${day}일 `;
        if (hour > 0 || day > 0) text += `${hour}시간 `;
        text += `${min}분`;

        el.innerText = text;
    }, 1000);
})();

function restoreOriginalPrices() {
    const rules = getRepresentativePriceRules(PARSED_SKUS);
    renderMainPriceTiers(rules);
    renderPriceTiers(rules);

    document.querySelectorAll('.sku-row').forEach(row => {
        const skuId = row.dataset.skuId;
        const qty = Number(row.querySelector('.qty-input')?.value || 0);

        const unitPrice = resolveUnitPriceByQty(skuId, qty);

        row.dataset.unitPrice = unitPrice;
        row.dataset.discountedUnitPrice = unitPrice;

        const priceEl = row.querySelector('.sku-price');
        if (priceEl) {
            priceEl.innerText = `₩${unitPrice.toLocaleString()}`;
        }
    });

    updateSummary();
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
    // 1️⃣ 바로 주문도 checkoutItems로 통일
    const items = [{
        skuId: skuId,
        quantity: quantity
    }];

    localStorage.setItem('checkoutItems', JSON.stringify(items));

    location.href = '/order/checkout';
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
