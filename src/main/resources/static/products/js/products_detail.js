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
        const loginLink = document.querySelector('a.link[href="/user/login"]');
        if (loginLink) {
            alert('로그인이 필요합니다.');
            window.location.href = '/user/login';
            return;
        }

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
            buyNow(items);
        } else {
            addToCart(items);
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
        row.dataset.skuId = sku.skuId;
        row.dataset.unitPrice = unitPrice;
        row.dataset.discountedUnitPrice = discounted;

        /* ===== row ===== */
        row.className =
            'sku-row flex justify-between items-center gap-4 ' +
            'p-3 border border-slate-200 bg-white';

        row.innerHTML = `
            <div class="sku-left flex flex-col gap-1">
                <div class="sku-id font-bold text-sm">${sku.skuId}</div>
                <div class="sku-options text-xs text-slate-600">
                    ${optionText}
                </div>
                <div class="sku-stock text-xs text-slate-500">
                    재고 ${sku.stockQuantity ?? 0}개
                </div>
            </div>

            <div class="sku-right text-right">
                <div class="sku-price font-bold mb-1">
                    ₩${discounted.toLocaleString()}
                </div>

                <div class="qty-box flex items-center gap-1">
                    <button class="qty-minus w-7 h-7 border border-slate-300
                                   bg-slate-50 font-bold">-</button>

                    <input class="qty-input w-10 h-7 text-center
                                  border border-slate-300"
                           type="number" min="0" value="0">

                    <button class="qty-plus w-7 h-7 border border-slate-300
                                  bg-slate-50 font-bold">+</button>
                </div>
            </div>
        `;

        container.appendChild(row);
    });

    bindSkuQtyEvents();
}




function renderAllSkuInfo() {
    const container = document.getElementById('mainSkuList');
    if (!container) return;

    if (!Array.isArray(PARSED_SKUS) || PARSED_SKUS.length === 0) {
        container.innerHTML =
            `<div class="text-slate-400 text-sm">SKU 정보 없음</div>`;
        return;
    }

    container.innerHTML = PARSED_SKUS.map(sku => {
        const option =
            sku.optionSummary && sku.optionSummary.trim() !== ''
                ? sku.optionSummary
                : '옵션 없음';

        return `
            <div class="flex items-start justify-between
                        border border-slate-200 rounded-lg
                        px-3 py-2 mb-2 text-sm">

                <div class="flex flex-col">
                    <span class="font-bold">${sku.skuId}</span>
                    <span class="text-slate-600 text-xs">
                        ${option}
                    </span>
                </div>

                <span class="text-xs text-slate-500">
                    재고 ${sku.stockQuantity ?? 0}개
                </span>
            </div>
        `;
    }).join('');
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
        li.className = 'flex justify-between items-end';

        li.innerHTML = `
            <span>
                ${r.maxQty ? `${r.minQty}~${r.maxQty}` : `${r.minQty}+`}개
            </span>

            ${
            discounted !== original
                ? `
                        <span class="text-right">
                            <div class="text-xs text-slate-400 line-through">
                                ₩${original.toLocaleString()}
                            </div>
                            <div class="font-bold text-red-600">
                                ₩${discounted.toLocaleString()}
                            </div>
                        </span>
                      `
                : `
                        <span class="font-bold">
                            ₩${original.toLocaleString()}
                        </span>
                      `
        }
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
        div.className = 'p-4 border border-slate-200 rounded-xl';

        div.innerHTML = `
            <div class="text-xs text-slate-500 mb-1">
                ${r.maxQty ? `${r.minQty}~${r.maxQty}` : `${r.minQty}+`}개
            </div>

            ${
            discounted !== original
                ? `
                        <del class="text-sm text-slate-400">
                            ₩${original.toLocaleString()}
                        </del>

                        <div class="text-lg font-bold text-red-600">
                            ₩${discounted.toLocaleString()}
                        </div>
                      `
                : `
                        <div class="text-lg font-bold">
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

function addToCart(items) {
    const requests = items.map(item => ({
        ...item,
        productId: PRODUCT_ID
    }));

    fetch('/api/cart/items', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requests)
    })
    .then(res => {
        if (!res.ok) {
            throw new Error('장바구니 추가에 실패했습니다. 다시 시도해주세요.');
        }
        alert('장바구니에 담겼습니다.');
        closeModal();
    })
    .catch(error => {
        console.error('Error adding items to cart:', error);
        alert(error.message);
    });
}
function buyNow(items) {
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
