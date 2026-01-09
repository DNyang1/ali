/* login.js */

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
        .then(async res => { // async 키워드 추가 (text() 대기를 위해)
            // 1. 성공 시 (200 OK)
            if (res.ok) {
                return res.json();
            }

            // 2. 에러 메시지 본문 꺼내기
            const msg = await res.text();

            // 3. 정지된 계정 (403 Forbidden)
            if (res.status === 403) {
                throw new Error(msg); // 서버가 보낸 "정지된 계정입니다..." 메시지로 에러 발생
            }

            // 4. 일반 로그인 실패 (401 Unauthorized 등)
            else {
                throw new Error("아이디 또는 비밀번호가 틀렸습니다.");
            }
        })
        .then(data => {
            if (data.status === "success") {
                alert("로그인 성공!");

                // 관리자 및 일반 유저 리다이렉트 로직 (기존 코드 유지)
                if (data.role && data.role.includes("ROLE_ADMIN")) {
                    location.href = "/admin/adminpage";
                    return;
                }
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
            // 여기서 에러 메시지를 alert로 출력
            // err.message에 서버에서 보낸 사유가 들어있음
            alert(err.message);
        });
}