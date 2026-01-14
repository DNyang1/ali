// 모달 바깥 배경 클릭 시 닫기
window.onclick = function(event) {
    const modal = document.getElementById('rejectModal');
    if (event.target == modal) {
        modal.style.display = "none";
    }
}