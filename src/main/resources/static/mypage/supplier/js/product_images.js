
function previewThumb(input) {
    const preview = document.getElementById("thumbPreview");
    if (!preview) return;

    const file = input?.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (e) => {
        preview.src = e.target.result;
        preview.style.display = "block";
    };
    reader.readAsDataURL(file);
}


async function readBodyAsText(res) {
    try {
        return await res.text();
    } catch (e) {
        return "";
    }
}

async function safeReadJson(res) {
    const ct = res.headers.get("content-type") || "";
    if (!ct.includes("application/json")) return null;
    try {
        return await res.json();
    } catch (e) {
        return null;
    }
}

function pickImagePathFromResponse(data) {
    if (!data) return null;

    if (Array.isArray(data)) {
        return data?.[0]?.imagePath || null;
    }

    if (typeof data === "object") {
        if (data.imagePath) return data.imagePath;
        if (Array.isArray(data.list)) return data.list?.[0]?.imagePath || null;
        if (Array.isArray(data.data)) return data.data?.[0]?.imagePath || null;
    }

    return null;
}


async function uploadThumb(productId) {
    const input = document.getElementById("thumbFile");
    const file = input?.files?.[0];
    if (!file) return alert("대표 이미지를 선택해주세요.");

    const fd = new FormData();
    fd.append("file", file);

    let res;
    try {
        res = await fetch(`/mypage/supplier/product/${productId}/images/thumb`, {
            method: "POST",
            body: fd,
        });
    } catch (e) {
        console.error(e);
        return alert("대표 이미지 업로드 실패 (네트워크)");
    }

    if (!res.ok) {
        const t = await readBodyAsText(res);
        console.error("thumb upload failed:", res.status, t);
        return alert("대표 이미지 업로드 실패");
    }

    const data = await safeReadJson(res);
    const path = pickImagePathFromResponse(data);

    if (path) {
        const preview = document.getElementById("thumbPreview");
        if (preview) {
            preview.src = path;
            preview.style.display = "block";
        }
    }

    input.value = "";

    alert("대표 이미지 저장 완료");

    if (!path) location.reload();
}


let selectedDetailFiles = [];

function onSelectDetailFiles(input) {
    if (!input?.files?.length) return;

    for (const f of input.files) {
        const key = `${f.name}_${f.size}`;
        if (!selectedDetailFiles.some((x) => `${x.name}_${x.size}` === key)) {
            selectedDetailFiles.push(f);
        }
    }

    input.value = "";
    renderDetailLocalPreview();
}

function renderDetailLocalPreview() {
    const wrap = document.getElementById("detailPreview");
    if (!wrap) return;

    wrap.innerHTML = "";

    selectedDetailFiles.forEach((file, idx) => {
        const box = document.createElement("div");
        box.style.cssText =
            "display:flex; flex-direction:column; align-items:center; gap:6px;";

        const img = document.createElement("img");
        img.style.cssText =
            "width:120px; height:120px; object-fit:cover; border:1px solid #ddd; border-radius:8px;";

        const reader = new FileReader();
        reader.onload = (e) => (img.src = e.target.result);
        reader.readAsDataURL(file);

        const btn = document.createElement("button");
        btn.type = "button";
        btn.textContent = "제거";

        btn.className =
            "danger inline-flex items-center justify-center rounded-xl bg-rose-600 px-3 py-2 text-xs font-bold text-white hover:bg-rose-700";
        btn.onclick = () => {
            selectedDetailFiles.splice(idx, 1);
            renderDetailLocalPreview();
        };

        box.appendChild(img);
        box.appendChild(btn);
        wrap.appendChild(box);
    });
}


async function uploadDetails(productId) {
    if (!selectedDetailFiles?.length) return alert("상세 이미지를 선택해주세요.");

    const fd = new FormData();

    selectedDetailFiles.forEach((f) => fd.append("files", f));

    let res;
    try {
        res = await fetch(`/mypage/supplier/product/${productId}/images/detail`, {
            method: "POST",
            body: fd,
        });
    } catch (e) {
        console.error(e);
        return alert("상세 이미지 업로드 실패 (네트워크)");
    }

    if (!res.ok) {
        const t = await readBodyAsText(res);
        console.error("detail upload failed:", res.status, t);
        return alert("상세 이미지 업로드 실패");
    }

    const data = await safeReadJson(res);

    selectedDetailFiles = [];
    const localWrap = document.getElementById("detailPreview");
    if (localWrap) localWrap.innerHTML = "";

    if (Array.isArray(data)) {
        renderDetailServerPreview(data);
        alert("상세 이미지 추가 완료");
        return;
    }

    alert("상세 이미지 추가 완료");
    location.reload();
}

function renderDetailServerPreview(list) {
    const wrap = document.getElementById("detailServerImages");
    if (!wrap) return;


    (list || []).forEach((row) => {
        if (!row?.imagePath) return;

        const box = document.createElement("div");
        box.className = "w-[200px]";

        const imgWrap = document.createElement("div");
        imgWrap.className =
            "relative w-[200px] h-[200px] overflow-hidden rounded-xl border border-slate-200 shadow-sm";

        const img = document.createElement("img");
        img.src = row.imagePath;
        img.className = "w-full h-full object-cover";
        img.alt = "상세 이미지";

        imgWrap.appendChild(img);
        box.appendChild(imgWrap);
        wrap.appendChild(box);
    });
}

async function deleteDetailImage(imageId) {
    if (!confirm("이 이미지를 삭제할까요?")) return;

    let res;
    try {
        res = await fetch(`/mypage/supplier/product/images/${imageId}`, {
            method: "DELETE",
        });
    } catch (e) {
        console.error(e);
        return alert("삭제 실패 (네트워크)");
    }

    if (res.ok) {
        location.reload();
    } else {
        console.error(await readBodyAsText(res));
        alert("삭제 실패");
    }
}
