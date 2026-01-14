document.addEventListener('DOMContentLoaded', () => {

    const categoryList = document.getElementById('categoryList');
    const allCategoryList = document.getElementById('allCategoryList');
    const allCategoryGrid = document.getElementById('allCategoryGrid');
    const allCategoryTitle = document.getElementById('allCategoryTitle');

    const overlay = document.getElementById('allCategoryOverlay');
    const panel = document.querySelector('.all-category-panel');
    const closeBtn = document.getElementById('categoryCloseBtn');
    const categoryBox = document.querySelector('.category-box');

    fetch('/api/categories/main')
        .then(res => res.json())
        .then(list => {
            if (!Array.isArray(list)) return;

            categoryList.innerHTML = '';
            allCategoryList.innerHTML = '';

            list.forEach(c => {
                const html = `
          <div class="flex items-center justify-between gap-2 px-3 py-2 rounded-lg
                      hover:bg-teal-50 cursor-pointer">
            <div class="flex items-center gap-2">
              <span class="w-7 h-7 grid place-items-center rounded-lg bg-teal-100 text-teal-600">📦</span>
              <span class="text-sm font-medium">${c.categoryName}</span>
            </div>
            <span class="text-slate-400">›</span>
          </div>
        `;

                const li1 = document.createElement('li');
                li1.innerHTML = html;
                li1.addEventListener('click', (e) => {
                    e.stopPropagation();
                    openOverlay(c.categoryId, c.categoryName);
                });
                categoryList.appendChild(li1);

                const li2 = document.createElement('li');
                li2.innerHTML = html;
                li2.addEventListener('click', () => {
                    openOverlay(c.categoryId, c.categoryName);
                });
                allCategoryList.appendChild(li2);
            });
        })
        .catch(console.error);

    function openOverlay(categoryId, categoryName) {
        overlay.classList.remove('hidden');
        allCategoryTitle.textContent = categoryName;
        allCategoryGrid.innerHTML = '<p class="text-slate-400">로딩중...</p>';

        fetch(`/api/categories/children?parentId=${categoryId}`)
            .then(res => res.json())
            .then(children => {
                if (children.length > 0) {
                    renderSubCategories(children);
                } else {
                    fetchProducts(categoryId);
                }
            })
            .catch(console.error);
    }

    function renderSubCategories(categories) {
        allCategoryGrid.innerHTML = categories.map(c => `
      <div
        class="cursor-pointer border border-slate-200 rounded-xl
               px-4 py-3 bg-white text-sm font-medium
               hover:border-teal-300 hover:bg-teal-50 transition"
        data-category-id="${c.categoryId}">
        <div class="flex justify-between items-center">
          <span>${c.categoryName}</span>
          <span class="text-slate-400">›</span>
        </div>
      </div>
    `).join('');

        allCategoryGrid.querySelectorAll('[data-category-id]')
            .forEach(el => {
                el.addEventListener('click', () => {
                    fetchProducts(el.dataset.categoryId);
                });
            });
    }

    async function fetchProducts(categoryId) {
        try {
            const res = await fetch(`/api/products/preview?category=${categoryId}`);
            const products = await res.json();

            console.log('preview products:', products);

            if (!Array.isArray(products) || products.length === 0) {
                allCategoryGrid.innerHTML = `
              <div class="col-span-full text-center text-slate-400 py-10">
                상품이 없습니다.
              </div>`;
                return;
            }

            allCategoryGrid.innerHTML = products.map(p => `
            <a href="/products/${p.productId}"
               class="group bg-slate-50 rounded-xl overflow-hidden border hover:shadow transition">
                <img
                    src="https://picsum.photos/seed/${p.productId}/300/200"
                    class="w-full h-32 object-cover">
                <div class="text-sm font-semibold text-slate-800 truncate">
                    ${p.productName}
                </div>
            </a>
        `).join('');
        } catch (e) {
            console.error(e);
            allCategoryGrid.innerHTML = '<p>오류 발생</p>';
        }
    }


    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) overlay.classList.add('hidden');
    });
    panel.addEventListener('click', e => e.stopPropagation());
    closeBtn?.addEventListener('click', () => overlay.classList.add('hidden'));

    categoryBox?.addEventListener('click', () => overlay.classList.remove('hidden'));

});

document.querySelectorAll('.category-horizontal-wrap')
    .forEach(el => {
        el.addEventListener('wheel', (e) => {
            e.preventDefault();
            el.scrollLeft += e.deltaY;
        }, { passive: false });
    });
