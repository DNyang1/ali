console.log("products_search.js 로드됨");

document.addEventListener('DOMContentLoaded', async () => {
    console.log("DOMContentLoaded 실행됨");

    const keywordEl = document.getElementById('search-keyword');
    const productsEl = document.getElementById('search-products');

    if (!keywordEl || !productsEl) {
        console.log("❌ 검색 데이터 없음");
        return;
    }

    const keyword = keywordEl.value;
    const products = JSON.parse(productsEl.value);

    console.log("검색어:", keyword);
    console.log("상품:", products);

    const aiProducts = products.slice(0, 10).map(p => ({
        name: p.productName,
        category: p.categoryName,
        custom: p.custom,
        priceRange: `${p.minPrice} ~ ${p.maxPrice}`,
        description: p.shortDesc
    }));

    const aiResult = await fetch(`/api/ai/search-summary?keyword=${keyword}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(aiProducts)
    }).then(res => res.text());

    console.log("AI 결과:", aiResult);

    if (aiResult) {
        document.getElementById('ai-search-box').style.display = 'block';
        document.getElementById('ai-result').innerText = aiResult;
    }
});

document.addEventListener('DOMContentLoaded', () => {

    const grid = document.querySelector('section.grid');
    const sortButtons = document.querySelectorAll('.sort-btn');

    if (!grid || sortButtons.length === 0) return;

    const originalOrder = Array.from(grid.querySelectorAll('.product-card'));

    let cards = [...originalOrder];

    const setActive = (activeBtn) => {
        sortButtons.forEach(b => {
            b.classList.remove('bg-slate-900', 'text-white');
            b.classList.add('bg-white', 'text-slate-600', 'border');
        });
        activeBtn.classList.remove('bg-white', 'text-slate-600', 'border');
        activeBtn.classList.add('bg-slate-900', 'text-white');
    };

    const render = (list) => {
        list.forEach(card => grid.appendChild(card));
        cards = [...list];
    };

    sortButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const type = btn.dataset.sort;
            setActive(btn);

            if (type === 'latest') {
                render(originalOrder);
                return;
            }

            if (type === 'price-asc') {
                const sorted = [...cards].sort((a, b) =>
                    (Number(a.dataset.minPrice) || 0) - (Number(b.dataset.minPrice) || 0)
                );
                render(sorted);
                return;
            }

            if (type === 'price-desc') {
                const sorted = [...cards].sort((a, b) =>
                    (Number(b.dataset.minPrice) || 0) - (Number(a.dataset.minPrice) || 0)
                );
                render(sorted);
            }
        });
    });
});

document.addEventListener('DOMContentLoaded', () => {

    const toggleBtn = document.getElementById('ai-toggle-btn');
    const toggleIcon = document.getElementById('ai-toggle-icon');
    const content = document.getElementById('ai-content');
    const aiBox = document.getElementById('ai-search-box');

    if (!toggleBtn || !content || !aiBox) return;

    let opened = true;

    toggleBtn.addEventListener('click', () => {
        opened = !opened;

        if (opened) {
            content.classList.remove('hidden');
            toggleBtn.firstChild.textContent = '접기';
            toggleIcon.classList.add('rotate-180');
        } else {
            content.classList.add('hidden');
            toggleBtn.firstChild.textContent = '펼치기';
            toggleIcon.classList.remove('rotate-180');
        }
    });
});
