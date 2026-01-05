    function sendResetLink() {
    const userId = document.getElementById('userId').value;
    const email = document.getElementById('email').value;

    if(!userId || !email) {
    alert("아이디와 이메일을 모두 입력해주세요.");
    return;
}

    fetch('/user/send_reset_link', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId: userId, email: email })
}).then(res => {
    if(res.ok) {
    alert("입력하신 이메일로 재설정 링크가 전송되었습니다. 메일함을 확인해주세요.");
    location.href = "/user/login";
} else {
    alert("일치하는 사용자 정보가 없습니다.");
}
});
}