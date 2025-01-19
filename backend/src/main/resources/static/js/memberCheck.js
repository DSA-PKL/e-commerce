document.addEventListener('DOMContentLoaded', function() {
    console.log('Script loaded');  // 스크립트 로드 확인
    
    // 에러 메시지 처리
    const error = document.getElementById('errorMessage')?.value;
    if(error) {
        alert(error);
    }

    // 이메일 체크 버튼에 이벤트 리스너 추가
    const emailBtn = document.getElementById('checkEmailBtn');
    console.log('Email button:', emailBtn);  // 버튼 엘리먼트 확인
    
    if (emailBtn) {
        emailBtn.addEventListener('click', checkEmailDuplicate);
    }
    
    // 전화번호 체크 버튼에 이벤트 리스너 추가
    const phoneBtn = document.getElementById('checkPhoneBtn');
    console.log('Phone button:', phoneBtn);  // 버튼 엘리먼트 확인
    
    if (phoneBtn) {
        phoneBtn.addEventListener('click', checkPhoneDuplicate);
    }
});

function checkEmailDuplicate() {
    const email = document.getElementById('email').value;
    if (!email) {
        alert('Please enter your email.');
        return;
    }
    
    console.log('Checking email:', email);
    
    fetch(`/api/members/check-email?email=${encodeURIComponent(email)}`)
        .then(response => {
            console.log('Response status:', response.status);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Response data:', data);
            const resultDiv = document.getElementById('emailResult');
            if (data.isAvailable) {
                resultDiv.innerHTML = '<div class="check-result valid"><i class="bi bi-check-circle"></i> This email is available.</div>';
            } else {
                resultDiv.innerHTML = '<div class="check-result invalid"><i class="bi bi-x-circle"></i> This email is already registered.</div>';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred while checking email availability.');
        });
}

function checkPhoneDuplicate() {
    const phoneNumber = document.getElementById('phoneNumber').value;
    if (!phoneNumber) {
        alert('Please enter your phone number.');
        return;
    }
    
    console.log('Checking phone:', phoneNumber);
    
    fetch(`/api/members/check-phone?phoneNumber=${encodeURIComponent(phoneNumber)}`)
        .then(response => {
            console.log('Response status:', response.status);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Response data:', data);
            const resultDiv = document.getElementById('phoneResult');
            if (data.isAvailable) {
                resultDiv.innerHTML = '<div class="check-result valid"><i class="bi bi-check-circle"></i> This phone number is available.</div>';
            } else {
                resultDiv.innerHTML = '<div class="check-result invalid"><i class="bi bi-x-circle"></i> This phone number is already registered.</div>';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred while checking phone number availability.');
        });
} 