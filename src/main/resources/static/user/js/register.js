function sendAuthCode() {
    const email = document.getElementById('email').value;
    if(!email) { alert("이메일을 입력해주세요."); return; }

    fetch('/user/send-auth-code?email=' + encodeURIComponent(email), { method: 'POST' })
        .then(res => {
            if(res.ok) {
                alert("인증번호가 발송되었습니다.");
                document.getElementById('authCodeSection').style.display = 'block';
            } else {
                alert("발송 실패");
            }
        });
}

// [추가] 2. 인증번호 검증 함수
function verifyAuthCode() {
    const code = document.getElementById('authCode').value;
    fetch('/user/verify-auth-code?code=' + encodeURIComponent(code), { method: 'POST' })
        .then(res => {
            if(res.ok) {
                alert("인증 완료!");
                document.getElementById('email').readOnly = true; // 이메일 수정 불가 처리
                document.getElementById('signupBtn').disabled = false;
            } else { alert("인증번호가 틀립니다."); }
        });
}

function signup() {
    // 1. input 태그들로부터 값을 가져와 data 객체를 정의합니다.
    const data = {
        userId: document.getElementById('userId').value,
        password: document.getElementById('password').value,
        name: document.getElementById('name').value,
        email: document.getElementById('email').value,
        phone: document.getElementById('phone').value,
        birth: document.getElementById('birth').value,
        address: document.getElementById('address').value
    };

    // 2. 정의된 data를 서버로 전송합니다.
    fetch('/user/signup', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    })
        .then(res => res.text())
        .then(msg => {
            if (msg === '회원가입 성공' || msg === 'success') {
                alert('가입 완료! 모든 정보가 DB에 저장되었습니다.');
                location.href = '/user/login'; // 가입 후 로그인 페이지로 이동
            } else {
                alert('가입 실패: ' + msg);
            }
        })
        .catch(err => {
            console.error('Error:', err);
            alert('서버 통신 중 오류가 발생했습니다.');
        });
}

