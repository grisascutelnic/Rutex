document.addEventListener('DOMContentLoaded', function() {
    const resetPasswordForm = document.getElementById('reset-password-form');
    
    if (resetPasswordForm) {
        resetPasswordForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const token = document.getElementById('token').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            
            // Basic validation
            if (!newPassword || !confirmPassword) {
                showNotification('Toate câmpurile sunt obligatorii', 'error');
                return;
            }
            
            if (newPassword !== confirmPassword) {
                showNotification('Parolele nu se potrivesc', 'error');
                return;
            }
            
            if (newPassword.length < 6) {
                showNotification('Parola trebuie să aibă cel puțin 6 caractere', 'error');
                return;
            }
            
            // Show loading state
            const submitBtn = document.getElementById('reset-btn');
            const originalText = submitBtn.innerHTML;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Se salvează...';
            submitBtn.disabled = true;
            
            // Make API call
            fetch('/api/auth/reset-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ 
                    token: token,
                    newPassword: newPassword 
                })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    showNotification(data.message, 'success');
                    
                    // Auto-login after successful password reset
                    setTimeout(() => {
                        const userEmail = data.email;
                        const newPassword = document.getElementById('newPassword').value;
                        
                        if (userEmail) {
                            // Auto-login with the new password
                            fetch('/api/auth/login', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/json',
                                },
                                body: JSON.stringify({ 
                                    email: userEmail,
                                    password: newPassword 
                                })
                            })
                            .then(loginResponse => {
                                if (loginResponse.ok) {
                                    return loginResponse.json();
                                } else {
                                    throw new Error('Login failed');
                                }
                            })
                            .then(loginData => {
                                if (loginData.success) {
                                    showNotification('Autentificare automată reușită!', 'success');
                                    // Redirect to home page
                                    setTimeout(() => {
                                        const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
                                        window.location.href = '/' + currentLang;
                                    }, 1000);
                                } else {
                                    // If auto-login fails, redirect to login page
                                    const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
                                    sessionStorage.setItem('passwordResetSuccess', 'true');
                                    window.location.href = '/' + currentLang + '/login';
                                }
                            })
                            .catch(error => {
                                console.error('Auto-login error:', error);
                                // If auto-login fails, redirect to login page
                                const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
                                sessionStorage.setItem('passwordResetSuccess', 'true');
                                window.location.href = '/' + currentLang + '/login';
                            });
                        } else {
                            // Fallback to login page
                            const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
                            sessionStorage.setItem('passwordResetSuccess', 'true');
                            window.location.href = '/' + currentLang + '/login';
                        }
                    }, 1500);
                } else {
                    showNotification(data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Reset password error:', error);
                showNotification('A apărut o eroare. Încearcă din nou.', 'error');
            })
            .finally(() => {
                // Reset button state
                submitBtn.innerHTML = originalText;
                submitBtn.disabled = false;
            });
        });
    }
});

function showNotification(message, type) {
    // Create notification element
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
    
    // Add to page
    document.body.appendChild(notification);
    
    // Show notification
    setTimeout(() => {
        notification.classList.add('show');
    }, 100);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 5000);
    
    // Close button functionality
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
