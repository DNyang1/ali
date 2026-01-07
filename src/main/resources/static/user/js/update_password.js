function submitPassword() {
    // .trim()을 추가하여 앞뒤 공백 제거
    const currentPw = document.getElementById('currentPassword').value.trim();
    const newPw = document.getElementById('newPassword').value.trim();
    const confirmPw = document.getElementById('confirmPassword').value.trim();

    if (!currentPw || !newPw || !confirmPw) {
        alert("모든 필드를 입력해주세요.");
        return;
    }
    if (newPw !== confirmPw) {
        alert("새 비밀번호가 일치하지 않습니다.");
        return;
    }

    const formData = new URLSearchParams();
    formData.append('currentPassword', currentPw);
    formData.append('newPassword', newPw);

    fetch('/user/update_password', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: formData
    }).then(res => {
        if (res.ok) {
            alert("비밀번호가 변경되었습니다. 다시 로그인해주세요.");
            location.href = "/user/login";
        } else {
            // 2. 현재 비밀번호 불일치 (서버 체크)
            alert("현재 비밀번호가 틀렸거나 오류가 발생했습니다.");
        }
    });
}