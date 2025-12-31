
(function () {
    const cat1 = document.getElementById('cat1');
    const cat2 = document.getElementById('cat2');
    const cat3 = document.getElementById('cat3');
    const hiddenCategoryId = document.getElementById('categoryId');

    if (!cat1 || !cat2 || !cat3 || !hiddenCategoryId) return;

    function reset(sel, disabled = true, placeholder = '선택') {
        sel.innerHTML = `<option value="">${placeholder}</option>`;
        sel.disabled = disabled;
    }

    async function loadChildren(parentId, target, placeholder) {
        const res = await fetch(`/mypage/supplier/category/children?parentId=${encodeURIComponent(parentId)}`);
        if (!res.ok) throw new Error('카테고리 조회 실패');
        const data = await res.json();

        reset(target, false, placeholder);
        data.forEach(c => {
            const opt = document.createElement('option');
            opt.value = c.categoryId;
            opt.textContent = c.categoryName;
            target.appendChild(opt);
        });
    }

    if (cat3.value) hiddenCategoryId.value = cat3.value;

    cat1.addEventListener('change', async () => {
        hiddenCategoryId.value = '';
        reset(cat2, true, '중분류 선택');
        reset(cat3, true, '소분류 선택');

        const v = cat1.value;
        if (!v) return;

        await loadChildren(v, cat2, '중분류 선택');
    });

    cat2.addEventListener('change', async () => {
        hiddenCategoryId.value = '';
        reset(cat3, true, '소분류 선택');

        const v = cat2.value;
        if (!v) return;

        await loadChildren(v, cat3, '소분류 선택');
    });

    cat3.addEventListener('change', () => {
        hiddenCategoryId.value = cat3.value || '';
    });
})();
