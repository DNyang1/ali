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
                const prevPage = document.referrer;

                if (prevPage && !prevPage.includes('/user/login')
                    && !prevPage.includes('/user/find_id')
                    && !prevPage.includes('/user/reset_pw')) {
                    location.href = prevPage;
                } else {
                    location.href = "/";
                }
            }
        })
        .catch(err => {
            alert("아이디 또는 비밀번호가 틀렸습니다.");
        });
}
