document.addEventListener('DOMContentLoaded', function() {
    // Check if user just reset their password
    const passwordResetSuccess = sessionStorage.getItem('passwordResetSuccess');
    if (passwordResetSuccess) {
        sessionStorage.removeItem('passwordResetSuccess');
        showNotification('Parola a fost resetată cu succes! Acum poți să te conectezi cu noua parolă.', 'success');
    }
    
    const loginForm = document.getElementById('login-form');
    
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const rememberMe = document.getElementById('remember-me').checked;
            
            // Basic validation
            if (!email || !password) {
                showNotification('Toate câmpurile sunt obligatorii', 'error');
                return;
            }
            
            // Show loading state
            const submitBtn = document.getElementById('login-btn');
            const originalText = submitBtn.innerHTML;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Se conectează...';
            submitBtn.disabled = true;
            
            // Make API call
            fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: email,
                    password: password,
                    rememberMe: rememberMe
                })
            })
            .then(response => {
                console.log('Login response status:', response.status);
                console.log('Login response headers:', response.headers);
                return response.json();
            })
            .then(data => {
                console.log('Login response data:', data);
                if (data.success) {
                    showNotification(data.message, 'success');
                    console.log('Login successful, updating navbar...');
                    
                    // Update navbar for logged in user
                    if (typeof checkAuthStatus === 'function') {
                        checkAuthStatus();
                    }
                    
                    // Check for redirect parameter and sessionStorage
                    const urlParams = new URLSearchParams(window.location.search);
                    const redirectTo = urlParams.get('redirect');
                    const redirectAfterLogin = sessionStorage.getItem('redirectAfterLogin');
                    const floatingButtonRedirect = sessionStorage.getItem('floatingButtonRedirect');
                    
                    // Redirect after successful login
                    setTimeout(() => {
                        console.log('🔄 Redirecting after successful login...');
                        console.log('📝 redirectTo from URL:', redirectTo);
                        console.log('📝 redirectAfterLogin from sessionStorage:', redirectAfterLogin);
                        console.log('📝 floatingButtonRedirect from sessionStorage:', floatingButtonRedirect);
                        
                        let targetUrl = null;
                        
                        // Prioritate 1: redirectTo din URL (pentru link-uri directe)
                        if (redirectTo) {
                            console.log('🎯 Using redirectTo from URL:', redirectTo);
                            targetUrl = redirectTo;
                        }
                        // Prioritate 2: redirectAfterLogin din sessionStorage (pentru butonul fluturător)
                        else if (redirectAfterLogin && floatingButtonRedirect) {
                            console.log('🎯 Using redirectAfterLogin from floating button:', redirectAfterLogin);
                            targetUrl = redirectAfterLogin;
                            sessionStorage.removeItem('redirectAfterLogin');
                            sessionStorage.removeItem('floatingButtonRedirect');
                        }
                        // Prioritate 3: redirectAfterLogin din sessionStorage (pentru navigare normală)
                        else if (redirectAfterLogin) {
                            console.log('🎯 Using redirectAfterLogin from sessionStorage:', redirectAfterLogin);
                            targetUrl = redirectAfterLogin;
                            sessionStorage.removeItem('redirectAfterLogin');
                        }
                        // Prioritate 4: pagina principală
                        else {
                            console.log('🎯 No redirect specified, going to home page');
                            const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
                            targetUrl = '/' + currentLang;
                        }
                        
                        // Verifică și adaugă prefix de limbă dacă este necesar
                        if (targetUrl && !targetUrl.startsWith('/ro/') && !targetUrl.startsWith('/ru/') && targetUrl !== '/' && !targetUrl.startsWith('/ro') && !targetUrl.startsWith('/ru')) {
                            const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
                            targetUrl = '/' + currentLang + targetUrl;
                        }
                        
                        console.log('🚀 Final redirect URL:', targetUrl);
                        window.location.href = targetUrl;
                    }, 1500);
                } else {
                    console.log('Login failed:', data.message);
                    showNotification(data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Error during login:', error);
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
