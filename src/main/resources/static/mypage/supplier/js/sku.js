
    let rangeIndex = 1;

    function addRangeRow() {
    const wrap = document.getElementById('rangeWrap');
    const row = document.createElement('div');
    row.className = 'range-row';
    row.style.cssText = 'display:flex; gap:8px; align-items:center; margin:6px 0;';

    row.innerHTML = `
      <input type="number" name="ranges[${rangeIndex}].min" placeholder="최소" min="1" style="width:90px;">
      <span>~</span>
      <input type="number" name="ranges[${rangeIndex}].max" placeholder="최대(비우면 무한)" min="1" style="width:120px;">
      <input type="number" name="ranges[${rangeIndex}].price" placeholder="가격" min="0" style="width:140px;">
      <button type="button" class="danger" onclick="removeRangeRow(this)">삭제</button>
    `;

    wrap.appendChild(row);
    rangeIndex++;
}

    function removeRangeRow(btn) {
    const row = btn.closest('.range-row');
    if (row) row.remove();
}
    function closeTierPanel() {
        document.getElementById("tierPanel").style.display = "none";
    }

    let openedSkuId = null;

    document.addEventListener("click", (e) => {
        const btn = e.target.closest(".tier-btn");
        if (!btn) return;

        const skuId = btn.dataset.skuId;
        const tr = btn.closest("tr");
        toggleTierAccordion(tr, skuId);
    });

    function closeAnyAccordion() {
        const old = document.querySelector("tr.tier-accordion");
        if (old) old.remove();
        openedSkuId = null;
    }

    async function toggleTierAccordion(anchorTr, skuId) {
        if (openedSkuId === skuId) {
            closeAnyAccordion();
            return;
        }

        closeAnyAccordion();
        openedSkuId = skuId;

        const colCount = anchorTr.children.length;
        const accTr = document.createElement("tr");
        accTr.className = "tier-accordion";
        accTr.innerHTML = `
            <td colspan="${colCount}">
            <div class="panel tier-panel">
            <div class="tier-head">
            <div class="tier-left">
            <b>구간단가</b>
        <span class="tier-sku">(${skuId})</span>
    </div>

        <div class="tier-center">
            기본가: <b class="js-base">-</b>
        </div>

        <div class="tier-right">
            <button type="button" class="danger js-close-tier">닫기</button>
        </div>
    </div>

        <table class="table tier-table">
            <thead>
            <tr><th>최소수량</th><th>최대수량</th><th>가격</th></tr>
            </thead>
            <tbody class="js-body">
            <tr><td colspan="3">불러오는 중...</td></tr>
            </tbody>
        </table>
    </div>
    </td>
        `;


        anchorTr.insertAdjacentElement("afterend", accTr);

        accTr.querySelector(".js-close-tier").addEventListener("click", () => {
            closeAnyAccordion();
        });

        try {
            const res = await fetch(`/mypage/supplier/sku/${encodeURIComponent(skuId)}/prices`);
            if (!res.ok) throw new Error(res.status);

            const raw = await res.json();
            if (!Array.isArray(raw)) throw new Error("Not array");

            const list = raw.filter(r => r && r.minQty != null && r.price != null);
            const toNum = (v) => (v == null ? null : Number(v));

            const baseRow = list.find(r => toNum(r.minQty) === 1 && (r.maxQty == null));
            accTr.querySelector(".js-base").textContent =
                baseRow ? toNum(baseRow.price).toLocaleString() : "-";

            const tiers = list.filter(r => r.maxQty != null);

            const body = accTr.querySelector(".js-body");
            body.innerHTML = "";

            if (tiers.length === 0) {
                body.innerHTML = `<tr><td colspan="3">구간단가 없음</td></tr>`;
            } else {
                for (const row of tiers) {
                    body.insertAdjacentHTML("beforeend", `
          <tr>
            <td>${row.minQty}</td>
            <td>${row.maxQty}</td>
            <td>${toNum(row.price).toLocaleString()}</td>
          </tr>
        `);
                }
            }
        } catch (err) {
            const body = accTr.querySelector(".js-body");
            body.innerHTML = `<tr><td colspan="3">불러오기 실패</td></tr>`;
            console.error(err);
        }
    }




