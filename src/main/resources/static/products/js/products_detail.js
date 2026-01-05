document.addEventListener('DOMContentLoaded', () => {

    const modal = document.getElementById('orderModal');
    const overlay = document.getElementById('orderModalOverlay');
    const openBtn = document.getElementById('openOrderModal');
    const closeBtn = document.getElementById('closeOrderModal');

    const qtyInput = document.getElementById('orderQty');
    const priceEl = document.getElementById('orderPrice');

    const selectedOptions = {};

    const rules = normalizePriceRules(DEFAULT_PRICE_RULES);


    renderMainPriceTiers(rules);

    openBtn?.addEventListener('click', () => {
        modal.style.display = 'block';
        overlay.style.display = 'block';
        document.body.style.overflow = 'hidden';

        renderPriceTiers(rules);

        updatePrice();
    });

    closeBtn?.addEventListener('click', closeModal);
    overlay?.addEventListener('click', closeModal);

    function closeModal() {
        modal.style.display = 'none';
        overlay.style.display = 'none';
        document.body.style.overflow = 'auto';
    }

    document.addEventListener('click', e => {
        if (!e.target.classList.contains('option-btn')) return;

        const box = e.target.closest('.option-box');
        if (!box) return;

        const name = box.querySelector('.option-title')?.innerText;
        const valueId = e.target.dataset.optionValueId;

        box.querySelectorAll('.option-btn')
            .forEach(b => b.classList.remove('active'));

        e.target.classList.add('active');
        selectedOptions[name] = valueId;
    });

    qtyInput?.addEventListener('input', updatePrice);

    function updatePrice() {
        const qty = Number(qtyInput.value || 1);
        const unitPrice = findUnitPriceByQty(rules, qty);

        priceEl.dataset.unitPrice = unitPrice;
        priceEl.innerText = (qty * unitPrice).toLocaleString() + '원';
    }

    document.getElementById('submitOrder')?.addEventListener('click', () => {
        console.log({
            quantity: qtyInput.value,
            options: selectedOptions
        });
        alert('주문 데이터 콘솔 확인');
    });
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


