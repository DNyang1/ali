function rejectWithMemo(supplierId) {
    const memo = prompt("반려 사유를 입력해주세요:");
    if (memo && memo.trim() !== "") {
        // 동적 폼 생성 및 전송
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/admin/supplier/reject';

        const idInput = document.createElement('input');
        idInput.type = 'hidden';
        idInput.name = 'supplierId';
        idInput.value = supplierId;

        const memoInput = document.createElement('input');
        memoInput.type = 'hidden';
        memoInput.name = 'memo';
        memoInput.value = memo;

        form.appendChild(idInput);
        form.appendChild(memoInput);
        document.body.appendChild(form);
        form.submit();
    }
}