
    let rangeIndex = 0;

    function getMoqValue() {
        const moqEl = document.querySelector('[name="moq"]');
        const v = Number(moqEl?.value);
        return Number.isFinite(v) && v > 0 ? v : 1;
    }

    function addRangeRow() {
        const wrap = document.getElementById("rangeWrap");
        if (!wrap) return;

        const moq = getMoqValue();
        const rows = Array.from(wrap.querySelectorAll(".range-row"));

        let nextMin = moq;

        if (rows.length > 0) {
            const lastRow = rows[rows.length - 1];
            const lastMaxEl = lastRow.querySelector(".js-range-max");
            const lastMaxVal = Number(lastMaxEl?.value);

            if (!Number.isFinite(lastMaxVal) || lastMaxVal < 1) {
                alert("바로 위 구간의 '최대'를 먼저 입력해야 다음 구간을 추가할 수 있습니다.");
                lastMaxEl?.focus();
                return;
            }
            nextMin = lastMaxVal + 1;
        }

        const row = document.createElement("div");
        row.className = "range-row";


        row.innerHTML = `
    <input type="number" class="js-range-min"
           name="ranges[${rangeIndex}].min"
           placeholder="최소" min="${moq}" value="${nextMin}">
    <span>~</span>
    <input type="number" class="js-range-max"
           name="ranges[${rangeIndex}].max"
           placeholder="최대(비우면 무한)" min="${moq}">
    <input type="number" class="js-range-price"
           name="ranges[${rangeIndex}].price"
           placeholder="가격" min="0">
    <button type="button" class="danger" onclick="removeRangeRow(this)">삭제</button>
  `;

        wrap.appendChild(row);
        rangeIndex++;

        row.querySelector(".js-range-max")?.focus();
    }

    function removeRangeRow(btn) {
        const row = btn.closest(".range-row");
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
        const moq = Number(btn.dataset.moq || 1);
        const tr = btn.closest("tr");
        toggleTierAccordion(tr, skuId, moq);
    });

    function closeAnyAccordion() {
        const old = document.querySelector("tr.tier-accordion");
        if (old) old.remove();
        openedSkuId = null;
    }

    async function toggleTierAccordion(anchorTr, skuId, initialMoq) {
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
            MOQ:
            <input type="number" min="1" step="1" class="js-moq-input tier-input" >
            기본가:
            <input type="number" min="0" step="1" class="js-base-input tier-input">
            <span class="js-min-amount tier-min-amount"></span>
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

        <div class="tier-foot">
          <button type="button" class="primary js-add-tier">+ 구간 추가</button>
          <small class="hint">* 구간은 겹치면 안 됩니다. (예: 10~49, 50~99)</small>
        </div>
      </div>
    </td>
  `;

        anchorTr.insertAdjacentElement("afterend", accTr);

        const moqInput = accTr.querySelector(".js-moq-input");
        const baseInput = accTr.querySelector(".js-base-input");
        const minAmountEl = accTr.querySelector(".js-min-amount");
        const body = accTr.querySelector(".js-body");

        const getCurrentMoq = () => {
            const v = Number(moqInput.value);
            return Number.isFinite(v) && v > 0 ? v : 1;
        };

        moqInput.value = (Number.isFinite(initialMoq) && initialMoq > 0) ? initialMoq : 1;

        const renderMinAmount = () => {
            const moq = getCurrentMoq();
            const price = Number(baseInput.value);
            if (Number.isFinite(price) && price > 0) {
                minAmountEl.textContent = `MOQ ${moq}개 · 최소주문금액 ${(moq * price).toLocaleString()}원`;
            } else {
                minAmountEl.textContent = `MOQ ${moq}개`;
            }
        };

        moqInput.addEventListener("input", () => {
            const moq = getCurrentMoq();
            accTr.querySelectorAll(".js-min, .js-max").forEach(inp => {
                inp.min = String(moq);
            });
            renderMinAmount();
        });
        baseInput.addEventListener("input", renderMinAmount);

        accTr.querySelector(".js-close-tier").addEventListener("click", closeAnyAccordion);

        accTr.addEventListener("click", (e) => {
            if (e.target.classList.contains("js-del-row")) {
                e.target.closest("tr")?.remove();
            }
        });

        accTr.querySelector(".js-add-tier").addEventListener("click", () => {
            addEmptyTierRow(body, getCurrentMoq());
        });

        accTr.querySelector(".js-save-tier").addEventListener("click", async () => {
            try {
                const moq = getCurrentMoq();
                const basePrice = Number(baseInput.value);

                if (!Number.isFinite(moq) || moq < 1) {
                    alert("MOQ는 1 이상이어야 합니다.");
                    return;
                }
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
                    if (min < moq) {
                        alert(`최소수량은 MOQ(${moq}) 이상이어야 합니다.`);
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

                const payload = { moq, basePrice, ranges };

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
                toggleTierAccordion(anchorTr, skuId, moq);
            } catch (err) {
                console.error(err);
                alert("저장 실패");
            }
        });

        try {
            const res = await fetch(`/mypage/supplier/sku/${encodeURIComponent(skuId)}/prices`);
            if (!res.ok) throw new Error(`HTTP ${res.status}`);

            const raw = await res.json();
            if (!Array.isArray(raw)) throw new Error("Not array");

            const list = raw.filter(r => r && r.minQty != null && r.unitPrice != null);

            const toNum = (v) => (v == null ? null : Number(v));
            const moq = getCurrentMoq();

            // 기본가: (maxQty=null) + (minQty=1 또는 minQty=MOQ)
            const baseRow = list.find(r => {
                if (r.maxQty != null) return false;
                const min = toNum(r.minQty);
                return min === 1 || min === moq;
            });

            const basePrice = baseRow ? toNum(baseRow.unitPrice) : null;
            baseInput.value = basePrice != null ? basePrice : "";
            renderMinAmount();

            const tiers = list
                .filter(r => r.maxQty != null)
                .sort((a, b) => Number(a.minQty) - Number(b.minQty));

            body.innerHTML = "";

            if (tiers.length === 0) {
                body.innerHTML = `<tr><td colspan="4">구간단가 없음</td></tr>`;
            } else {
                for (const row of tiers) {
                    body.insertAdjacentHTML(
                        "beforeend",
                        tierRowHtml(row.minQty, row.maxQty, row.unitPrice, moq)
                    );
                }
            }
        } catch (err) {
            body.innerHTML = `<tr><td colspan="4">불러오기 실패</td></tr>`;
            console.error("prices fetch fail:", err);
        }
    }


    function tierRowHtml(minQty, maxQty, price, minLimit = 1) {
        const min = Number(minQty || 0);
        const p = Number(price || 0);
        const amountText = (min > 0 && p > 0) ? (min * p).toLocaleString() : "-";

        return `
    <tr class="js-tier-row">
      <td>
        <input type="number"
               class="js-min tier-num"
               min="${minLimit}"
               step="1"
               value="${minQty ?? ''}">
      </td>
      <td>
        <input type="number"
               class="js-max tier-num"
               min="${minLimit}"
               step="1"
               value="${maxQty ?? ''}">
      </td>
      <td>
        <input type="number"
               class="js-price tier-price"
               min="0"
               step="1"
               value="${price ?? ''}">
        <div class="tier-amount">
          최소금액: ${amountText}원
        </div>
      </td>
      <td><button type="button" class="danger js-del-row">삭제</button></td>
    </tr>
  `;
    }

    function addEmptyTierRow(bodyEl, moq) {
        const onlyMsg = bodyEl.querySelector("tr td[colspan]");
        if (onlyMsg) bodyEl.innerHTML = "";

        const safeMoq = (Number.isFinite(moq) && moq > 0) ? moq : 1;
        const rows = Array.from(bodyEl.querySelectorAll("tr.js-tier-row"));
        const lastWithMax = [...rows].reverse().find(r => {
            const mv = Number(r.querySelector(".js-max")?.value);
            return Number.isFinite(mv) && mv > 0;
        });

        const nextMin = lastWithMax ? (Number(lastWithMax.querySelector(".js-max").value) + 1) : safeMoq;
        const lastRow = rows[rows.length - 1];
        if (lastRow) {
            const minEl = lastRow.querySelector(".js-min");
            const maxEl = lastRow.querySelector(".js-max");
            const priceEl = lastRow.querySelector(".js-price");

            const isEmpty =
                (!minEl.value || minEl.value.trim() === "") &&
                (!maxEl.value || maxEl.value.trim() === "") &&
                (!priceEl.value || priceEl.value.trim() === "");

            if (isEmpty) {
                minEl.min = String(safeMoq);
                maxEl.min = String(safeMoq);
                minEl.value = String(nextMin);
                maxEl.focus();
                return;
            }
        }

        bodyEl.insertAdjacentHTML("beforeend", tierRowHtml(nextMin, "", "", safeMoq));
    }

    (function(){
        const bar = document.getElementById("optToggleBar");
        const btn = document.getElementById("optToggleBtn");
        const sec = document.getElementById("optSection");
        if (!bar || !btn || !sec) return;

        const groups = sec.querySelectorAll(".opt-group").length;

        if (groups <= 2){
            bar.style.display = "none";
            sec.classList.remove("is-collapsed");
            return;
        }

        const KEY = "supplier_opt_open";

        const apply = (open) => {
            sec.classList.toggle("is-collapsed", !open);
            btn.textContent = open ? "옵션 접기" : "옵션 펼치기";
            btn.setAttribute("aria-expanded", String(open));
            localStorage.setItem(KEY, open ? "1" : "0");
        };

        apply(localStorage.getItem(KEY) === "1");

        btn.addEventListener("click", () => {
            const isOpen = !sec.classList.contains("is-collapsed");
            apply(!isOpen);
        });
    })();


