
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
         기본가:
            <input type="number" min="0" step="1" class="js-base-input" style="width:120px;">
            <span class="js-min-amount" style="margin-left:12px; opacity:.7;"></span>
        </div>

        <div class="tier-right">
          <button type="button" class="primary js-save-tier">저장</button>
          <button type="button" class="danger js-close-tier">닫기</button>
        </div>
      </div>

      <table class="table tier-table">
        <thead>
          <tr><th>최소수량</th><th>최대수량</th><th>가격</th><th>삭제</th></tr>
        </thead>
        <tbody class="js-body">
          <tr><td colspan="4">불러오는 중...</td></tr>
        </tbody>
      </table>

      <div style="margin-top:8px; display:flex; gap:8px; align-items:center;">
        <button type="button" class="primary js-add-tier">+ 구간 추가</button>
        <small class="hint">* 구간은 겹치면 안 됩니다. (예: 10~49, 50~99)</small>
      </div>
    </div>
  </td>
`;


        anchorTr.insertAdjacentElement("afterend", accTr);

        accTr.querySelector(".js-close-tier").addEventListener("click", () => {
            closeAnyAccordion();
        });
        accTr.querySelector(".js-add-tier").addEventListener("click", () => {
            const body = accTr.querySelector(".js-body");
            addEmptyTierRow(body);
        });

        accTr.addEventListener("click", (e) => {
            if (e.target.classList.contains("js-del-row")) {
                const tr = e.target.closest("tr");
                if (tr) tr.remove();
            }
        });

        accTr.querySelector(".js-save-tier").addEventListener("click", async () => {
            try {
                const basePrice = Number(accTr.querySelector(".js-base-input").value);
                if (!Number.isFinite(basePrice) || basePrice <= 0) {
                    alert("기본가를 0보다 크게 입력하세요.");
                    return;
                }

                const rows = Array.from(accTr.querySelectorAll("tr.js-tier-row"));
                const ranges = [];

                for (const r of rows) {
                    const min = Number(r.querySelector(".js-min").value);
                    const max = Number(r.querySelector(".js-max").value);
                    const price = Number(r.querySelector(".js-price").value);

                    if (!Number.isFinite(min) || !Number.isFinite(max) || !Number.isFinite(price)) {
                        alert("구간단가 입력값을 확인하세요.");
                        return;
                    }
                    if (min < 1 || max < 1 || min > max) {
                        alert("구간 범위가 올바르지 않습니다. (min <= max)");
                        return;
                    }
                    if (price <= 0) {
                        alert("구간 가격은 0보다 커야 합니다.");
                        return;
                    }

                    ranges.push({ minQty: min, maxQty: max, price });
                }

                ranges.sort((a, b) => a.minQty - b.minQty);
                for (let i = 1; i < ranges.length; i++) {
                    if (ranges[i].minQty <= ranges[i - 1].maxQty) {
                        alert("구간이 서로 겹칩니다. 구간을 다시 확인하세요.");
                        return;
                    }
                }

                const payload = { basePrice, ranges };

                const res = await fetch(`/mypage/supplier/sku/${encodeURIComponent(skuId)}/prices`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                });

                if (!res.ok) {
                    const msg = await res.text();
                    alert(msg || "저장 실패");
                    return;
                }

                alert("저장 완료");
                closeAnyAccordion();
                toggleTierAccordion(anchorTr, skuId);

            } catch (err) {
                console.error(err);
                alert("저장 실패");
            }
        });
        try {
            const res = await fetch(`/mypage/supplier/sku/${encodeURIComponent(skuId)}/prices`);
            if (!res.ok) throw new Error(res.status);

            const raw = await res.json();
            if (!Array.isArray(raw)) throw new Error("Not array");

            const moq = Number(raw[0]?.moq ?? 1);

            const list = raw.filter(r => r && r.minQty != null && r.price != null);
            const toNum = (v) => (v == null ? null : Number(v));

            const baseRow = list.find(r => toNum(r.minQty) === 1 && (r.maxQty == null));
            const basePrice = baseRow ? toNum(baseRow.price) : null;

            const baseInput = accTr.querySelector(".js-base-input");
            baseInput.value = basePrice != null ? basePrice : "";

            const minAmountEl = accTr.querySelector(".js-min-amount");
            if (basePrice != null) {
                minAmountEl.textContent = `(MOQ ${moq}개 최소금액: ${(moq * basePrice).toLocaleString()}원)`;
            } else {
                minAmountEl.textContent = `(MOQ ${moq}개)`;
            }

            const tiers = list
                .filter(r => r.maxQty != null)
                .sort((a, b) => Number(a.minQty) - Number(b.minQty));

            const body = accTr.querySelector(".js-body");
            body.innerHTML = "";

            if (tiers.length === 0) {
                body.innerHTML = `<tr><td colspan="4">구간단가 없음</td></tr>`;
            } else {
                for (const row of tiers) {
                    body.insertAdjacentHTML("beforeend", tierRowHtml(row.minQty, row.maxQty, row.price));
                }
            }

        } catch (err) {
            const body = accTr.querySelector(".js-body");
            body.innerHTML = `<tr><td colspan="4">불러오기 실패</td></tr>`; // ✅ colspan 4
            console.error(err);
        }
    }
    function tierRowHtml(minQty, maxQty, price) {
        const min = Number(minQty || 0);
        const p = Number(price || 0);
        const amountText = (min > 0 && p > 0) ? (min * p).toLocaleString() : "-";

        return `
    <tr class="js-tier-row">
      <td><input type="number" class="js-min" min="1" step="1" style="width:90px;" value="${minQty ?? ''}"></td>
      <td><input type="number" class="js-max" min="1" step="1" style="width:110px;" value="${maxQty ?? ''}"></td>
      <td>
        <input type="number" class="js-price" min="0" step="1" style="width:130px;" value="${price ?? ''}">
        <div style="font-size:11px; opacity:.65; margin-top:4px;">
          최소금액: ${amountText}원
        </div>
      </td>
      <td><button type="button" class="danger js-del-row">삭제</button></td>
    </tr>
  `;
    }

    function addEmptyTierRow(bodyEl) {
        const onlyMsg = bodyEl.querySelector("tr td[colspan]");
        if (onlyMsg) bodyEl.innerHTML = "";
        bodyEl.insertAdjacentHTML("beforeend", tierRowHtml("", "", ""));
    }



