function updateInfo() {
    const data = {
        name: document.getElementById('name').value,
        email: document.getElementById('email').value,
        phone: document.getElementById('phone').value,
        address: document.getElementById('address').value
    };

    fetch('/user/update', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    })
        .then(res => {
            if (res.ok) {
                alert('정보가 수정되었습니다.');
                location.href = '/user/index';
            } else {
                alert('수정 실패');
            }
        })
        .catch(err => console.error('Error:', err));
}
