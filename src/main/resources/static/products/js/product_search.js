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
