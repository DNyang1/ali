// 인증 완료 여부를 체크하는 변수
let isBizVerified = false;

/** 1. 국세청 API를 통해 사업자 번호의 진위를 확인하고 임시 인증번호를 발송(시뮬레이션)합니다. */
async function requestBizVerify() {
    const cpNumber = document.getElementById('cpNumber').value;

    if (!cpNumber || cpNumber.length < 10) {
        alert("올바른 사업자 등록번호 10자리를 입력해주세요.");
        return;
    }

    // 서버의 SupplierApiController로 진위 확인 요청
    const formData = new FormData();
    formData.append("cpNumber", cpNumber);

    try {
        const response = await fetch('/supplier/api/verify-biz', {
            method: 'POST',
            body: formData
        });

        const result = await response.json();

        if (result.status === "success") {
            // 성공 시 인증번호 입력란을 보여줌
            alert("유효한 사업자입니다. 인증번호[" + result.tempCode + "]를 입력하세요.");
            document.getElementById('bizAuthSection').style.display = 'block';

            // 번호 수정 방지 (인증 시도 중에는 고정)
            document.getElementById('cpNumber').readOnly = true;
        } else {
            alert("국세청에 등록되지 않았거나 폐업한 사업자 번호입니다.");
        }
    } catch (error) {
        console.error("Error:", error);
        alert("인증 서비스 통신 중 오류가 발생했습니다.");
    }
}

/**
 * 2. 사용자가 입력한 인증번호가 서버 세션에 저장된 번호와 일치하는지 확인합니다.
 */
async function confirmBizCode() {
    const code = document.getElementById('bizAuthCode').value;

    if (!code) {
        alert("인증번호를 입력해주세요.");
        return;
    }

    try {
        const response = await fetch('/supplier/api/confirm-code?code=' + code, {
            method: 'POST'
        });

        if (response.ok) {
            alert("사업자 인증이 완료되었습니다!");
            isBizVerified = true;

            // 인증 성공 시 UI 처리
            document.getElementById('bizAuthSection').style.display = 'none';
            const cpNumInput = document.getElementById('cpNumber');
            cpNumInput.style.backgroundColor = '#e9ecef';
            cpNumInput.readOnly = true;

            // 인증 버튼 비활성화 (선택 사항)
            const verifyBtn = document.querySelector("button[onclick='requestBizVerify()']");
            if(verifyBtn) verifyBtn.disabled = true;

        } else {
            alert("인증번호가 일치하지 않습니다. 다시 확인해주세요.");
        }
    } catch (error) {
        console.error("Error:", error);
        alert("인증 확인 중 오류가 발생했습니다.");
    }
}

/**
 * 3. 최종 판매자 등록 함수
 * 인증이 완료된 경우에만 서버로 가입 데이터를 전송합니다.
 */
function registerSupplier() {
    // 인증 여부 체크
    if (!isBizVerified) {
        alert("사업자 번호 인증을 먼저 완료해 주세요.");
        return;
    }

    const data = {
        cpName: document.getElementById('cpName').value,
        cpNumber: document.getElementById('cpNumber').value,
        cpPhone: document.getElementById('cpPhone').value,
        cpAddress: document.getElementById('cpAddress').value
    };

    if (!data.cpName || !data.cpPhone || !data.cpAddress) {
        alert("모든 정보를 입력해주세요.");
        return;
    }

    fetch('/user/supplier-signup', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    }).then(res => {
        if (res.ok) {
            alert("판매자 등록이 완료되었습니다!");
            location.href = "/mypage/supplier/dashboard";
        } else {
            alert("등록 실패: 데이터 형식을 확인하거나 관리자에게 문의하세요.");
        }
    }).catch(error => {
        console.error("Error:", error);
        alert("서버 통신 중 오류가 발생했습니다.");
    });
}