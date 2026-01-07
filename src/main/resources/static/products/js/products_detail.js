const selectedOptions = {};
let modalMode = null;
const PARSED_SKUS = typeof SKUS === 'string' ? JSON.parse(SKUS) : SKUS;

function closeModal() {
    const modal = document.getElementById('orderModal');
    const overlay = document.getElementById('orderModalOverlay');

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

    const qtyInput = document.getElementById('orderQty');
    const priceEl = document.getElementById('orderPrice');


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
        Object.keys(selectedOptions).forEach(key => delete selectedOptions[key]);
        document.querySelectorAll('.option-btn.active')
            .forEach(btn => btn.classList.remove('active'));

        modal.style.display = 'block';
        overlay.style.display = 'block';
        document.body.style.overflow = 'hidden';

        renderPriceTiers(rules);
        updatePrice();

        updateModalButtons();
    }
    function updateModalButtons() {
        const orderBtn = document.getElementById('submitOrder');
        const cartBtn = document.getElementById('submitCart');

        if (modalMode === 'ORDER') {
            orderBtn.style.display = 'block';
            cartBtn.style.display = 'none';
        } else {
            orderBtn.style.display = 'none';
            cartBtn.style.display = 'block';
        }
    }
    closeBtn?.addEventListener('click', closeModal);
    overlay?.addEventListener('click', closeModal);



    document.addEventListener('click', e => {
        const btn = e.target.closest('.option-btn');
        if (!btn) return;

        const box = btn.closest('.option-box');
        if (!box) return;

        const name = box.querySelector('.option-title')?.innerText?.trim();
        const valueId = btn.dataset.optionValueId;

        console.log('[OPTION CLICK]', { name, valueId });

        // 기존 active 처리
        box.querySelectorAll('.option-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        selectedOptions[name] = valueId;
        console.log('[selectedOptions NOW]', JSON.stringify(selectedOptions));
    });

    qtyInput?.addEventListener('input', updatePrice);

    function updatePrice() {
        const qty = Number(qtyInput.value || 1);
        const unitPrice = findUnitPriceByQty(rules, qty);

        priceEl.dataset.unitPrice = unitPrice;
        priceEl.innerText = (qty * unitPrice).toLocaleString() + '원';
    }

    document.getElementById('submitOrder')?.addEventListener('click', () => {
        handleSubmit('ORDER');
    });

    document.getElementById('submitCart')?.addEventListener('click', () => {
        handleSubmit('CART');
    });
    function handleSubmit(type) {

        const skuId = resolveSkuId();

        if (!skuId) {
            alert('옵션을 모두 선택해주세요.');
            return;
        }

        const quantity = Number(qtyInput.value);

        if (quantity < MOQ) {
            alert(`최소 주문 수량은 ${MOQ}개입니다.`);
            return;
        }

        if (type === 'CART') {
            addToCart(skuId, quantity);
        }

        if (type === 'ORDER') {
            buyNow(skuId, quantity);
        }
    }

});


function normalizePriceRules(data) {
    if (Array.isArray(data)) return data;

    if (typeof data === 'string') {
        try {
            return JSON.parse(data);
        } catch (e) {
            console.error('가격 JSON 파싱 실패', e);
            return [];
        }
    }
    return [];
}

function renderPriceTiers(priceRules) {
    const container = document.getElementById('priceTiers');
    container.innerHTML = '';

    if (!priceRules || priceRules.length === 0) {
        container.innerHTML = '<li>가격 정보 없음</li>';
        return;
    }

    priceRules.forEach(rule => {
        const min = Number(rule.minQty);
        const max = rule.maxQty !== null ? Number(rule.maxQty) : null;
        const price = Number(rule.unitPrice);

        const rangeText = max
            ? `${min} - ${max} 개`
            : `${min} 개 이상`;

        const li = document.createElement('li');
        li.className = 'price-tier-item';

        li.innerHTML = `
            <span>${rangeText}</span>
            <span>₩${price.toLocaleString()}</span>
        `;

        container.appendChild(li);
    });
}

function renderMainPriceTiers(priceRules) {
    const container = document.getElementById('mainPriceTiers');
    if (!container) return;

    container.innerHTML = '';

    if (!priceRules || priceRules.length === 0) {
        container.innerHTML = '<span>가격 정보 없음</span>';
        return;
    }

    priceRules.forEach(rule => {
        const min = Number(rule.minQty);
        const max = rule.maxQty !== null ? Number(rule.maxQty) : null;
        const price = Number(rule.unitPrice);

        const rangeText = max
            ? `${min} - ${max} 개`
            : `≥ ${min} 개`;

        const div = document.createElement('div');
        div.className = 'main-price-tier';

        div.innerHTML = `
            <div class="range">${rangeText}</div>
            <div class="price">₩${price.toLocaleString()}</div>
        `;

        container.appendChild(div);
    });
}

function findUnitPriceByQty(priceRules, qty) {

    // ✅ minQty 큰 순서로 정렬
    const sortedRules = [...priceRules].sort(
        (a, b) => Number(b.minQty) - Number(a.minQty)
    );

    for (const rule of sortedRules) {
        const min = Number(rule.minQty);
        const max = rule.maxQty !== null ? Number(rule.maxQty) : Infinity;

        if (qty >= min && qty <= max) {
            return Number(rule.unitPrice);
        }
    }

    return 0;
}
function resolveSkuId() {
    const selectedValueIds = Object.values(selectedOptions);

    if (selectedValueIds.length === 0) return null;

    for (const sku of PARSED_SKUS) {
        const skuOptionIds = sku.optionValueIds || [];

        // ✅ 핵심 수정 포인트
        const matched = skuOptionIds.every(id =>
            selectedValueIds.includes(id)
        );

        if (matched) {
            return sku.skuId;
        }
    }
    return null;
}
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
