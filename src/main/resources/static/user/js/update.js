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
                location.href = '/mypage/user/dashboard';
            } else {
                alert('수정 실패');
            }
        });
}

function previewImage(input) {
    const preview = document.getElementById('profilePreview');

    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            // 1. 이미지 소스를 읽어온 데이터로 교체
            preview.src = e.target.result;
            // 2. 숨겨져 있던 이미지 태그를 다시 보이게 설정 (block 또는 inline)
            preview.style.display = "block";
        }
        reader.readAsDataURL(input.files[0]);
    } else {
        // 파일을 선택하려다 취소한 경우 등을 대비해 다시 숨길 수도 있음
        preview.src = "";
        preview.style.display = "none";
    }
}

// 프로필 제거
function deleteProfileImage() {
    if (!confirm("프로필 이미지를 삭제하시겠습니까?")) return;

    fetch('/user/delete_profile_img', {
        method: 'POST'
    })
        .then(res => {
            if (res.ok) {
                const preview = document.getElementById('profilePreview');
                // 1. src를 비워서 이미지를 없앰 (회색 배경이 드러남)
                preview.src = "";
                // 2. ★ 중요: display: none 부분을 삭제하거나 block으로 유지 ★
                preview.style.display = "block";

                // 파일 입력칸도 비워줌
                document.getElementById('profileFile').value = "";
                alert("이미지가 삭제되었습니다.");
            } else {
                alert("이미지 삭제 실패");
            }
        })
        .catch(err => console.error('Error:', err));
}
