function registerSupplier() {
    const data = {
        cpName: document.getElementById('cpName').value,
        cpNumber: document.getElementById('cpNumber').value,
        cpPhone: document.getElementById('cpPhone').value,
        cpAddress: document.getElementById('cpAddress').value
    };

    fetch('/user/supplier-signup', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    }).then(res => {
        if (res.ok) {
            alert("판매자 등록이 완료되었습니다!");
            location.href = "/supplier/index";
        }
    });
}