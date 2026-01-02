function updateInfo() {
    const formData = new FormData();

    const userData = {
        name: document.getElementById('name').value,
        email: document.getElementById('email').value,
        phone: document.getElementById('phone').value,
        address: document.getElementById('address').value
    };

    // 1. 이미지 파일 추가 (input ID가 profileFile 인지 확인!)
    const fileInput = document.getElementById('profileFile');
    if (fileInput && fileInput.files[0]) {
        formData.append("profileFile", fileInput.files[0]);
    }

    // 2. JSON 데이터를 Blob으로 감싸서 추가
    formData.append("userData", new Blob([JSON.stringify(userData)], {type: "application/json"}));

    fetch('/user/update', {
        method: 'POST',
        body: formData // Content-Type 헤더는 절대 설정하지 마세요!
    })
        .then(res => {
            if (res.ok) {
                alert('정보가 수정되었습니다.');
                location.href = '/user/setting';
            } else {
                alert('수정 실패');
            }
        });
}

function previewImage(input) {
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            document.getElementById('profilePreview').src = e.target.result;
        }
        reader.readAsDataURL(input.files[0]);
    }
}