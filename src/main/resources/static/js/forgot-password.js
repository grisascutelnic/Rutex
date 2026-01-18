document.addEventListener('DOMContentLoaded', function() {
    const forgotPasswordForm = document.getElementById('forgot-password-form');
    
    if (forgotPasswordForm) {
        forgotPasswordForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const email = document.getElementById('email').value;
            
            // Basic validation
            if (!email) {
                showNotification('Introduceți adresa de email', 'error');
                return;
            }
            
            // Show loading state
            const submitBtn = document.getElementById('reset-btn');
            
            // Prevent multiple submissions
            if (submitBtn.disabled) {
                return;
            }
            
            const originalText = submitBtn.innerHTML;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Se trimite...';
            submitBtn.disabled = true;
            
            // Prepare request data
            const requestData = { email: email };
            
                    // reCAPTCHA v2 - get response
        if (typeof grecaptcha !== 'undefined') {
            const recaptchaResponse = grecaptcha.getResponse();
            if (recaptchaResponse) {
                requestData.recaptchaResponse = recaptchaResponse;
                // Continue with form submission
                submitForgotPasswordForm(requestData, submitBtn, originalText);
            } else {
                showNotification('Vă rugăm să completați reCAPTCHA.', 'error');
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalText;
            }
        } else {
            // If reCAPTCHA is not available, submit without it
            submitForgotPasswordForm(requestData, submitBtn, originalText);
        }
        });
    }
});

// reCAPTCHA callback function
function onRecaptchaSuccess(token) {
    console.log('reCAPTCHA completed successfully');
}

// Submit forgot password form (for reCAPTCHA v2)
async function submitForgotPasswordForm(requestData, submitBtn, originalText) {
    try {
        const response = await fetch('/api/auth/forgot-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestData)
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
        if (data.success) {
            showNotification(data.message, 'success');
            // Clear the form
            document.getElementById('forgot-password-form').reset();
            // Reset reCAPTCHA
            if (typeof grecaptcha !== 'undefined') {
                grecaptcha.reset();
            }
        } else {
            showNotification(data.message, 'error');
            // Reset reCAPTCHA on error
            if (typeof grecaptcha !== 'undefined') {
                grecaptcha.reset();
            }
        }
    } catch (error) {
        console.error('Forgot password error:', error);
        showNotification('A apărut o eroare. Încearcă din nou.', 'error');
    } finally {
        // Reset button state
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
    }
}

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
