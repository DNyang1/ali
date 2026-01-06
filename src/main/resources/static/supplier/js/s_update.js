/**
 * 공급자(사업자) 정보 수정 함수
 */
function updateSupplierInfo() {
    const supplierData = {
        cpNumber: document.getElementById('cpNumber').value,
        cpName: document.getElementById('cpName').value,
        cpPhone: document.getElementById('cpPhone').value,
        cpAddress: document.getElementById('cpAddress').value
    };

    if (!supplierData.cpName || !supplierData.cpPhone || !supplierData.cpAddress) {
        alert("모든 정보를 입력해 주세요.");
        return;
    }

    if (!confirm("업체 정보를 수정하시겠습니까?")) return;

    // 경로를 컨트롤러와 정확히 맞춤
    fetch('/supplier/s_update', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(supplierData)
    })
        .then(res => {
            if (res.ok) {
                alert('공급자 정보가 성공적으로 수정되었습니다.');
                // 컨트롤러의 GetMapping 경로인 /s_setting으로 수정
                location.href = '/supplier/s_setting';
            } else {
                alert('수정 실패: 서버 오류가 발생했습니다.');
            }
        })
        .catch(err => {
            console.error('Error:', err);
            alert('네트워크 오류가 발생했습니다.');
        });
}