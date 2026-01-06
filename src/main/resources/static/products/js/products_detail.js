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
    // 로컬에서만 쓰고 싶으면 주석 해제
    // if (location.hostname !== 'localhost') return;

    // 🔥 전역에 명시적으로 고정
    window.__DEV_FORCED_SKU_ID__ = null;

    document.addEventListener('DOMContentLoaded', () => {
        if (!Array.isArray(PARSED_SKUS) || PARSED_SKUS.length === 0) return;

        const select = document.createElement('select');
        select.style.cssText =
            'width:100%;margin-top:12px;padding:6px;font-size:13px;';

        select.innerHTML = '<option value="">[DEV] SKU 직접 선택 (장바구니/주문 공통)</option>';

        PARSED_SKUS.forEach(sku => {
            const opt = document.createElement('option');
            opt.value = sku.skuId;
            opt.textContent = `${sku.skuId} | ${sku.optionValueIds?.join(', ')}`;
            select.appendChild(opt);
        });

        select.addEventListener('change', e => {
            window.__DEV_FORCED_SKU_ID__ = e.target.value || null;
            console.log('🔧 [DEV] __DEV_FORCED_SKU_ID__ =', window.__DEV_FORCED_SKU_ID__);
        });

        document.body.appendChild(select);
    });

    // 🔥 resolveSkuId를 안전하게 후킹
    const originalResolveSkuId = window.resolveSkuId;

    window.resolveSkuId = function () {
        // ✅ 1순위: DEV 강제 SKU (ORDER / CART 공통)
        if (window.__DEV_FORCED_SKU_ID__) {
            return window.__DEV_FORCED_SKU_ID__;
        }

        // ✅ 2순위: 기존 옵션 기반 로직
        return originalResolveSkuId();
    };
})();
