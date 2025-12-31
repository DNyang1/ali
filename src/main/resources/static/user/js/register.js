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