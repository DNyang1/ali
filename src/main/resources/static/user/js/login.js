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
            // 성공 시 JSON 형태로 파싱하여 다음 then으로 넘깁니다.
            if (res.ok) return res.json();
            else throw new Error('로그인 실패');
        })
        .then(data => {
            // 서버에서 보낸 response.put("status", "success") 확인
            if (data.status === "success") {
                alert("로그인 성공!");

                // 1. 관리자 권한 확인: DB에 ROLE_ADMIN으로 저장된 경우
                // 문자열에 "ROLE_ADMIN"이 포함되어 있는지 체크합니다.
                if (data.role && data.role.includes("ROLE_ADMIN")) {
                    location.href = "/admin/adminpage";
                    return; // 관리자면 여기서 로직 종료 (홈으로 이동 방지)
                }

                // 2. 일반 유저(ROLE_BUYER 등) 이동 로직
                const prevPage = document.referrer;
                if (prevPage && !prevPage.includes('/user/login')
                    && !prevPage.includes('/user/register')
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