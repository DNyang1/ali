let rangeIndex = 0;

function getMoqValue() {
    const moqEl = document.querySelector('[name="moq"]');
    const v = Number(moqEl?.value);
    return Number.isFinite(v) && v > 0 ? v : 1;
}

// Tailwind용 버튼/인풋 기본 클래스(선택: 디자인 통일용)
// CSS 완전 제거할 거면 이거 쓰는 게 편함.
const TW = {
    input:
        "rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-blue-500",
    btnGhost:
        "inline-flex items-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50",
    btnPrimary:
        "inline-flex items-center rounded-xl bg-blue-600 px-3 py-2 text-xs font-bold text-white hover:bg-blue-700",
    btnDanger:
        "inline-flex items-center rounded-xl bg-rose-600 px-3 py-2 text-xs font-bold text-white hover:bg-rose-700",
};

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
    row.className = "range-row flex flex-wrap items-center gap-2";

    row.innerHTML = `
    <input type="number" class="js-range-min ${TW.input}"
           name="ranges[${rangeIndex}].min"
           placeholder="최소" min="${moq}" value="${nextMin}" style="width:110px;">
    <span class="text-slate-500">~</span>
    <input type="number" class="js-range-max ${TW.input}"
           name="ranges[${rangeIndex}].max"
           placeholder="최대(비우면 무한)" min="${moq}" style="width:160px;">
    <input type="number" class="js-range-price ${TW.input}"
           name="ranges[${rangeIndex}].price"
           placeholder="가격" min="0" style="width:160px;">
    <button type="button" class="${TW.btnDanger}" onclick="removeRangeRow(this)">삭제</button>
  `;

    wrap.appendChild(row);
    rangeIndex++;

    row.querySelector(".js-range-max")?.focus();
}

function removeRangeRow(btn) {
    const row = btn.closest(".range-row");
    if (row) row.remove();
}

let openedSkuId = null;

// ✅ tier-btn 클릭 바인딩은 class 유지 (네 HTML에도 tier-btn 유지했지?)
document.addEventListener("click", (e) => {
    const btn = e.target.closest(".tier-btn");
    if (!btn) return;

    const skuId = btn.dataset.skuId;
    const moq = Number(btn.dataset.moq || 1);
    const tr = btn.closest("tr");
    if (!skuId || !tr) return;

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
    <td colspan="${colCount}" class="bg-slate-50">
      <div class="mt-3 mb-4 rounded-2xl border border-slate-200 bg-white shadow-sm overflow-hidden">
        <div class="flex flex-col gap-3 border-b border-slate-100 px-4 py-3 md:flex-row md:items-center md:justify-between">
          <div class="flex items-center gap-2">
            <b class="text-sm text-slate-900">구간단가</b>
            <span class="text-xs text-slate-500">(${skuId})</span>
          </div>

          <div class="flex flex-wrap items-center gap-2 text-xs text-slate-600">
            <span class="font-bold text-slate-700">MOQ:</span>
            <input type="number" min="1" step="1" class="js-moq-input ${TW.input}" style="width:90px;">
            <span class="font-bold text-slate-700">기본가:</span>
            <input type="number" min="0" step="1" class="js-base-input ${TW.input}" style="width:120px;">
            <span class="js-min-amount text-xs text-slate-500"></span>
          </div>

          <div class="flex items-center gap-2">
            <button type="button" class="js-save-tier ${TW.btnPrimary}">저장</button>
            <button type="button" class="js-close-tier ${TW.btnDanger}">닫기</button>
          </div>
        </div>

        <div class="px-4 py-4">
          <div class="overflow-x-auto rounded-xl border border-slate-200">
            <table class="min-w-full text-sm bg-white">
              <thead>
                <tr class="text-left text-slate-500">
                  <th class="px-3 py-2 font-semibold">최소수량</th>
                  <th class="px-3 py-2 font-semibold">최대수량</th>
                  <th class="px-3 py-2 font-semibold">가격</th>
                  <th class="px-3 py-2 font-semibold">삭제</th>
                </tr>
              </thead>
              <tbody class="js-body divide-y divide-slate-100">
                <tr><td class="px-3 py-3 text-slate-500" colspan="4">불러오는 중...</td></tr>
              </tbody>
            </table>
          </div>

          <div class="mt-3 flex flex-wrap items-center justify-between gap-2">
            <button type="button" class="js-add-tier ${TW.btnPrimary}">+ 구간 추가</button>
            <small class="text-xs text-slate-500">* 구간은 겹치면 안 됩니다. (예: 10~49, 50~99)</small>
          </div>
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

    moqInput.value = Number.isFinite(initialMoq) && initialMoq > 0 ? initialMoq : 1;

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
        accTr.querySelectorAll(".js-min, .js-max").forEach((inp) => {
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
                body: JSON.stringify(payload),
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

    // 초기 로드
    try {
        const res = await fetch(`/mypage/supplier/sku/${encodeURIComponent(skuId)}/prices`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        const raw = await res.json();
        if (!Array.isArray(raw)) throw new Error("Not array");

        const list = raw.filter((r) => r && r.minQty != null && r.unitPrice != null);

        const toNum = (v) => (v == null ? null : Number(v));
        const moq = getCurrentMoq();

        const baseRow = list.find((r) => {
            if (r.maxQty != null) return false;
            const min = toNum(r.minQty);
            return min === 1 || min === moq;
        });

        const basePrice = baseRow ? toNum(baseRow.unitPrice) : null;
        baseInput.value = basePrice != null ? basePrice : "";
        renderMinAmount();

        const tiers = list
            .filter((r) => r.maxQty != null)
            .sort((a, b) => Number(a.minQty) - Number(b.minQty));

        body.innerHTML = "";

        if (tiers.length === 0) {
            body.innerHTML = `<tr><td class="px-3 py-3 text-slate-500" colspan="4">구간단가 없음</td></tr>`;
        } else {
            for (const row of tiers) {
                body.insertAdjacentHTML("beforeend", tierRowHtml(row.minQty, row.maxQty, row.unitPrice, moq));
            }
        }
    } catch (err) {
        body.innerHTML = `<tr><td class="px-3 py-3 text-slate-500" colspan="4">불러오기 실패</td></tr>`;
        console.error("prices fetch fail:", err);
    }
}

function tierRowHtml(minQty, maxQty, price, minLimit = 1) {
    const min = Number(minQty || 0);
    const p = Number(price || 0);
    const amountText = min > 0 && p > 0 ? (min * p).toLocaleString() : "-";

    return `
    <tr class="js-tier-row">
      <td class="px-3 py-2">
        <input type="number"
               class="js-min ${TW.input}"
               min="${minLimit}"
               step="1"
               value="${minQty ?? ""}" style="width:110px;">
      </td>
      <td class="px-3 py-2">
        <input type="number"
               class="js-max ${TW.input}"
               min="${minLimit}"
               step="1"
               value="${maxQty ?? ""}" style="width:140px;">
      </td>
      <td class="px-3 py-2">
        <input type="number"
               class="js-price ${TW.input}"
               min="0"
               step="1"
               value="${price ?? ""}" style="width:140px;">
        <div class="mt-1 text-xs text-slate-500">
          최소금액: ${amountText}원
        </div>
      </td>
      <td class="px-3 py-2">
        <button type="button" class="js-del-row ${TW.btnDanger}">삭제</button>
      </td>
    </tr>
  `;
}

function addEmptyTierRow(bodyEl, moq) {
    const onlyMsg = bodyEl.querySelector("tr td[colspan]");
    if (onlyMsg) bodyEl.innerHTML = "";

    const safeMoq = Number.isFinite(moq) && moq > 0 ? moq : 1;
    const rows = Array.from(bodyEl.querySelectorAll("tr.js-tier-row"));

    const lastWithMax = [...rows].reverse().find((r) => {
        const mv = Number(r.querySelector(".js-max")?.value);
        return Number.isFinite(mv) && mv > 0;
    });

    const nextMin = lastWithMax ? Number(lastWithMax.querySelector(".js-max").value) + 1 : safeMoq;

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

/**
 * ✅ 옵션 펼치기/접기 (Tailwind: hidden 기반)
 * - 기존 .is-collapsed / .opt-group 의존 제거
 * - "옵션 그룹 카드" 개수 기준으로 자동 숨김 처리
 */
(function () {
    const bar = document.getElementById("optToggleBar");
    const btn = document.getElementById("optToggleBtn");
    const sec = document.getElementById("optSection");
    if (!bar || !btn || !sec) return;

    // Tailwind 버전: optSection 안에 그룹 카드가 "보통 rounded-2xl border ..." 구조로 있음
    // 너무 빡세게 class에 의존하지 않게, "라벨+radio가 포함된 블록"을 그룹으로 카운트
    const guessGroupCount = () => {
        // 1) 네가 만든 그룹 카드: optSection 내부에 th:each로 생긴 카드들
        //    라디오 input이 들어있는 컨테이너를 기준으로 대략 그룹 수 추정
        const cards = sec.querySelectorAll('input[type="radio"]');
        if (cards.length === 0) return 0;

        // 그룹은 'selected[그룹키]' name 패턴으로 묶임 → name 기준 유니크 카운트
        const names = new Set();
        cards.forEach((r) => {
            if (r.name) names.add(r.name);
        });
        return names.size;
    };

    const groups = guessGroupCount();

    // 그룹이 적으면 토글바 숨기고 항상 펼침
    if (groups <= 2) {
        bar.style.display = "none";
        sec.classList.remove("hidden");
        btn.textContent = "옵션 접기";
        btn.setAttribute("aria-expanded", "true");
        return;
    }

    const KEY = "supplier_opt_open";

    const apply = (open) => {
        sec.classList.toggle("hidden", !open); // ✅ hidden 기반
        btn.textContent = open ? "옵션 접기" : "옵션 펼치기";
        btn.setAttribute("aria-expanded", String(open));
        localStorage.setItem(KEY, open ? "1" : "0");
    };

    // 초기 상태
    apply(localStorage.getItem(KEY) === "1");

    btn.addEventListener("click", () => {
        const isOpen = !sec.classList.contains("hidden");
        apply(!isOpen);
    });
})();

// 중복 SKU alert 유지
document.addEventListener("DOMContentLoaded", function () {
    const params = new URLSearchParams(window.location.search);
    if (params.get("error") === "dup") {
        alert("이미 동일한 옵션 조합 SKU가 존재합니다.");
    }
});
