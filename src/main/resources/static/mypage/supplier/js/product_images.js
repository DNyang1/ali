function previewThumb(input) {
    const preview = document.getElementById('thumbPreview');
    if (!preview) return;

    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = e => {
            preview.src = e.target.result;
            preview.style.display = "block";
        };
        reader.readAsDataURL(input.files[0]);
    }
}

function uploadThumb(productId) {
    const input = document.getElementById('thumbFile');
    if (!input || !input.files || !input.files[0]) return alert("대표 이미지를 선택해주세요.");

    const fd = new FormData();
    fd.append("file", input.files[0]);

    fetch(`/mypage/supplier/product/${productId}/images/thumb`, {
        method: "POST",
        body: fd
    })
        .then(r => r.ok ? r.json() : r.text().then(t => Promise.reject(t)))
        .then(list => {
            const path = (list && list[0] && list[0].imagePath) ? list[0].imagePath : null;
            if (path) {
                const preview = document.getElementById('thumbPreview');
                if (preview) {
                    preview.src = path;
                    preview.style.display = "block";
                }
            }

            input.value = "";

            alert("대표 이미지 저장 완료");
        })
        .catch(err => {
            console.error(err);
            alert("대표 이미지 업로드 실패");
        });
}

let selectedDetailFiles = [];

function onSelectDetailFiles(input) {
    if (!input || !input.files || input.files.length === 0) return;

    for (const f of input.files) {
        const key = `${f.name}_${f.size}`;
        if (!selectedDetailFiles.some(x => `${x.name}_${x.size}` === key)) {
            selectedDetailFiles.push(f);
        }
    }

    input.value = "";
    renderDetailLocalPreview();
}

function renderDetailLocalPreview() {
    const wrap = document.getElementById('detailPreview');
    if (!wrap) return;

    wrap.innerHTML = "";

    selectedDetailFiles.forEach((file, idx) => {
        const box = document.createElement("div");
        box.style.cssText = "display:flex; flex-direction:column; align-items:center; gap:6px;";

        const img = document.createElement("img");
        img.style.cssText =
            "width:120px; height:120px; object-fit:cover; border:1px solid #ddd; border-radius:8px;";

        const reader = new FileReader();
        reader.onload = e => img.src = e.target.result;
        reader.readAsDataURL(file);

        const btn = document.createElement("button");
        btn.type = "button";
        btn.textContent = "제거";
        btn.className = "danger";
        btn.onclick = () => {
            selectedDetailFiles.splice(idx, 1);
            renderDetailLocalPreview();
        };

        box.appendChild(img);
        box.appendChild(btn);
        wrap.appendChild(box);
    });
}

function uploadDetails(productId) {
    if (!selectedDetailFiles || selectedDetailFiles.length === 0) {
        return alert("상세 이미지를 선택해주세요.");
    }

    const fd = new FormData();
    selectedDetailFiles.forEach(f => fd.append("files", f));

    fetch(`/mypage/supplier/product/${productId}/images/detail`, {
        method: "POST",
        body: fd
    })
        .then(r => r.ok ? r.json() : r.text().then(t => Promise.reject(t)))
        .then(list => {
            selectedDetailFiles = [];
            const localWrap = document.getElementById('detailPreview');
            if (localWrap) localWrap.innerHTML = "";

            renderDetailServerPreview(list);
            alert("상세 이미지 추가 완료");
        })
        .catch(err => {
            console.error(err);
            alert("상세 이미지 업로드 실패");
        });
}

function renderDetailServerPreview(list) {
    const wrap = document.getElementById('detailServerImages');
    if (!wrap) return;

    wrap.innerHTML = "";

    (list || []).forEach(row => {
        if (!row || !row.imagePath) return;

        const img = document.createElement("img");
        img.src = row.imagePath;
        img.style.cssText =
            "width:120px; height:120px; object-fit:cover; border:1px solid #ddd; border-radius:8px; margin:6px;";
        wrap.appendChild(img);
    });
}

function deleteDetailImage(imageId) {
    if (!confirm("이 이미지를 삭제할까요?")) return;

    fetch(`/mypage/supplier/product/images/${imageId}`, {
        method: "DELETE"
    })
        .then(r => {
            if (r.ok) {
                location.reload();
            } else {
                alert("삭제 실패");
            }
        });
}

