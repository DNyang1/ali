fetch('/api/categories/main')
    .then(res => {
        if (!res.ok) {
            throw new Error('API 호출 실패');
        }
        return res.json();
    })
    .then(list => {
        console.log('category list:', list);

        if (!Array.isArray(list)) {
            console.error('배열 아님:', list);
            return;
        }

        const categoryList = document.getElementById('categoryList');
        const allCategoryList = document.getElementById('allCategoryList');

        categoryList.innerHTML = '';
        allCategoryList.innerHTML = '';

        list.forEach(c => {
            const html = `
                <span class="left">
                    <span class="icon">📦</span>
                    <span class="text">${c.categoryName}</span>
                </span>
                <span class="arrow">›</span>
            `;

            const li1 = document.createElement('li');
            li1.className = 'category-item';
            li1.innerHTML = html;
            categoryList.appendChild(li1);

            const li2 = document.createElement('li');
            li2.className = 'category-item';
            li2.innerHTML = html;
            allCategoryList.appendChild(li2);
        });
    })
    .catch(err => console.error(err));


const allCategoryBtn = document.getElementById('allCategoryBtn');
const overlay = document.getElementById('allCategoryOverlay');
const panel = document.querySelector('.all-category-panel');
const closeBtn = document.getElementById('categoryCloseBtn');
const categoryBox = document.querySelector('.category-box');

if (allCategoryBtn && overlay && panel) {

    allCategoryBtn.addEventListener('click', e => {
        e.stopPropagation();
        overlay.classList.toggle('hidden');
    });

    overlay.addEventListener('click', () => {
        overlay.classList.add('hidden');
    });

    panel.addEventListener('click', e => {
        e.stopPropagation();
    });

    closeBtn.addEventListener('click', () => {
        overlay.classList.add('hidden');
    });

}

if (categoryBox && overlay) {
    categoryBox.addEventListener('click', () => {
        overlay.classList.remove('hidden');
    });
}

document.querySelectorAll('.explore-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        window.location.href = '/products';
    });
});

document.addEventListener('DOMContentLoaded', () => {

    const exploreBtns = document.querySelectorAll('.explore-btn');

    exploreBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const custom = btn.dataset.custom;
            location.href = `/products/list?custom=${custom}`;
        });
    });

});


