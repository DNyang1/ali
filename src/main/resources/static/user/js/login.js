function login() {
    const data = {
        userId: document.getElementById('userId').value,
        password: document.getElementById('password').value
    };

    fetch('/user/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    })
        .then(res => {
            if (res.ok) return res.text(); // 성공 시 "success" 텍스트 추출
            else throw new Error('로그인 실패');
        })
        .then(msg => {
            if (msg === "success") {
                alert("로그인 성공!");
                // 이 코드가 핵심입니다. 루트(/) 즉, index 페이지로 이동시킵니다.
                location.href = "/user/index";
            }
        })
        .catch(err => {
            alert("아이디 또는 비밀번호가 틀렸습니다.");
        });
}