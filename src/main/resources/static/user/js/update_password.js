function submitPassword() {
    const newPw = document.getElementById('newPassword').value;
    const confirmPw = document.getElementById('confirmPassword').value;

    if (newPw !== confirmPw) {
        alert("새 비밀번호가 일치하지 않습니다.");
        return;
    }

    const formData = new URLSearchParams();
    formData.append('currentPassword', document.getElementById('currentPassword').value);
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
            alert("현재 비밀번호가 틀렸거나 오류가 발생했습니다.");
        }
    });
}