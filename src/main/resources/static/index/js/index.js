/* ===============================
   카테고리 데이터 로드
================================ */
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

        const categoryList = document.getElementById('categoryList');       // 좌측 고정
        const allCategoryList = document.getElementById('allCategoryList'); // 전체 패널

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

            // 좌측 카테고리
            const li1 = document.createElement('li');
            li1.className = 'category-item';
            li1.innerHTML = html;
            categoryList.appendChild(li1);

            // 전체 카테고리 패널
            const li2 = document.createElement('li');
            li2.className = 'category-item';
            li2.innerHTML = html;
            allCategoryList.appendChild(li2);
        });
    })
    .catch(err => console.error(err));


/* ===============================
   모든 카테고리 토글
================================ */
const allCategoryBtn = document.getElementById('allCategoryBtn');
const overlay = document.getElementById('allCategoryOverlay');
const panel = document.querySelector('.all-category-panel');
const closeBtn = document.getElementById('categoryCloseBtn');
const categoryBox = document.querySelector('.category-box');

if (allCategoryBtn && overlay && panel) {

    // 버튼 클릭 → 토글
    allCategoryBtn.addEventListener('click', e => {
        e.stopPropagation();
        overlay.classList.toggle('hidden');
    });

    // 바깥 클릭 → 닫기
    overlay.addEventListener('click', () => {
        overlay.classList.add('hidden');
    });

    // 패널 클릭 → 닫힘 방지
    panel.addEventListener('click', e => {
        e.stopPropagation();
    });

    // X 버튼 → 닫기
    closeBtn.addEventListener('click', () => {
        overlay.classList.add('hidden');
    });

}

if (categoryBox && overlay) {
    categoryBox.addEventListener('click', () => {
        overlay.classList.remove('hidden');
    });
}


