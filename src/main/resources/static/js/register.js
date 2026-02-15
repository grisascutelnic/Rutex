document.addEventListener('DOMContentLoaded', function() {
    const registerForm = document.getElementById('register-form');

    if (registerForm) {
        registerForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirm-password').value;
            const firstName = document.getElementById('firstName').value;
            const lastName = document.getElementById('lastName').value;

            if (!email || !password || !confirmPassword || !firstName || !lastName) {
                showNotification('Toate câmpurile sunt obligatorii', 'error');
                return;
            }

            if (password !== confirmPassword) {
                showNotification('Parolele nu se potrivesc', 'error');
                return;
            }

            if (password.length < 6) {
                showNotification('Parola trebuie să aibă cel puțin 6 caractere', 'error');
                return;
            }

            const termsCheckbox = document.getElementById('terms');
            if (!termsCheckbox || !termsCheckbox.checked) {
                showNotification('Trebuie să fiți de acord cu termenii și condițiile', 'error');
                return;
            }

            const submitBtn = document.getElementById('register-btn');
            if (submitBtn.disabled) {
                return;
            }

            const originalText = submitBtn.innerHTML;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Se înregistrează...';
            submitBtn.disabled = true;

            const formData = new URLSearchParams();
            formData.append('email', email);
            formData.append('password', password);
            formData.append('firstName', firstName);
            formData.append('lastName', lastName);

            const recaptchaEnabled = document.body?.dataset?.recaptchaEnabled === 'true';
            if (recaptchaEnabled) {
                if (typeof grecaptcha !== 'undefined') {
                    const recaptchaResponse = grecaptcha.getResponse();
                    if (recaptchaResponse) {
                        formData.append('recaptchaResponse', recaptchaResponse);
                        submitRegistrationForm(formData, submitBtn, originalText);
                    } else {
                        showNotification('Vă rugăm să completați reCAPTCHA.', 'error');
                        submitBtn.disabled = false;
                        submitBtn.innerHTML = originalText;
                    }
                } else {
                    showNotification('reCAPTCHA nu este disponibilă momentan.', 'error');
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = originalText;
                }
            } else {
                submitRegistrationForm(formData, submitBtn, originalText);
            }
        });
    }
});

async function submitRegistrationForm(formData, submitBtn, originalText) {
    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: formData
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();

        if (data.success) {
            showNotification(data.message, 'success');
            if (typeof grecaptcha !== 'undefined') {
                grecaptcha.reset();
            }
            if (typeof checkAuthStatus === 'function') {
                checkAuthStatus();
            }

            const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
            const redirectAfterLogin = sessionStorage.getItem('redirectAfterLogin');
            const floatingButtonRedirect = sessionStorage.getItem('floatingButtonRedirect');

            setTimeout(() => {
                let targetUrl;

                if (data.phoneCompletionRequired) {
                    targetUrl = '/' + currentLang + '/edit-profile?forcePhone=true';
                } else if (redirectAfterLogin && floatingButtonRedirect) {
                    targetUrl = redirectAfterLogin;
                    sessionStorage.removeItem('redirectAfterLogin');
                    sessionStorage.removeItem('floatingButtonRedirect');
                } else if (redirectAfterLogin) {
                    targetUrl = redirectAfterLogin;
                    sessionStorage.removeItem('redirectAfterLogin');
                } else {
                    targetUrl = '/' + currentLang;
                }

                if (targetUrl && !targetUrl.startsWith('/ro/') && !targetUrl.startsWith('/ru/') && targetUrl !== '/' && !targetUrl.startsWith('/ro') && !targetUrl.startsWith('/ru')) {
                    targetUrl = '/' + currentLang + targetUrl;
                }

                window.location.href = targetUrl;
            }, 1200);
        } else {
            showNotification(data.message, 'error');
            if (typeof grecaptcha !== 'undefined') {
                grecaptcha.reset();
            }
        }
    } catch (error) {
        console.error('Registration error details:', error);
        showNotification('A apărut o eroare. Încearcă din nou.', 'error');
    } finally {
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
    }
}

function showNotification(message, type) {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerHTML = `
        <div class="notification-content ${type}">
            <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
            <span>${message}</span>
            <button class="notification-close">
                <i class="fas fa-times"></i>
            </button>
        </div>
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.classList.add('show');
    }, 100);

    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 5000);

    const closeBtn = notification.querySelector('.notification-close');
    closeBtn.addEventListener('click', () => {
        notification.classList.remove('show');
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    });
}
