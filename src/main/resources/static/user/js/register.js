fetch('/user/signup', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(data)
})
    .then(res => res.text())
    .then(msg => {
        // UserController에서 ResponseEntity.ok("success")로 반환할 경우
        if (msg === 'success' || msg === '회원가입 성공') {
            alert('가입 완료! 모든 정보가 DB에 저장되었습니다.');
            location.href = '/user/index';
        } else {
            alert('가입 실패: ' + msg);
        }
    })
    .catch(err => console.error('Error:', err));
