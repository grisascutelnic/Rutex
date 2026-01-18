document.addEventListener('DOMContentLoaded', function() {
    console.log('Register.js loaded');
    initializePhoneInput();
    const registerForm = document.getElementById('register-form');
    console.log('Register form found:', registerForm);
    
    // Image upload functionality
    const profileImageInput = document.getElementById('profile-image');
    const currentProfileImage = document.getElementById('current-profile-image');
    const currentDefaultAvatar = document.getElementById('current-default-avatar');
    const removeImageBtn = document.getElementById('remove-image-btn');
    
    console.log('Profile image input found:', profileImageInput);
    console.log('Current profile image found:', currentProfileImage);
    console.log('Current default avatar found:', currentDefaultAvatar);
    console.log('Remove image button found:', removeImageBtn);
    
    if (profileImageInput) {
        profileImageInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            console.log('Image selected:', file);
            if (file) {
                console.log('File name:', file.name);
                console.log('File size:', file.size);
                console.log('File type:', file.type);
                // Show preview
                const reader = new FileReader();
                reader.onload = function(e) {
                    console.log('File preview loaded');
                    currentProfileImage.src = e.target.result;
                    currentProfileImage.style.display = 'block';
                    currentDefaultAvatar.style.display = 'none';
                    removeImageBtn.style.display = 'inline-flex';
                };
                reader.readAsDataURL(file);
            }
        });
    }
    
    if (removeImageBtn) {
        removeImageBtn.addEventListener('click', function() {
            // Clear the file input
            profileImageInput.value = '';
            // Hide preview and show default avatar
            currentProfileImage.style.display = 'none';
            currentDefaultAvatar.style.display = 'block';
            removeImageBtn.style.display = 'none';
        });
    }
    
    if (registerForm) {
        // Phone validation is now handled by intl-tel-input
        
        registerForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirm-password').value;
            const firstName = document.getElementById('firstName').value;
            const lastName = document.getElementById('lastName').value;
            const phone = document.getElementById('phone').value;
            const profileImage = document.getElementById('profile-image').files[0];
            
            // Basic validation
            if (!email || !password || !confirmPassword || !firstName || !lastName || !phone) {
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
            
            // Validare termeni și condiții
            const termsCheckbox = document.getElementById('terms');
            if (!termsCheckbox || !termsCheckbox.checked) {
                showNotification('Trebuie să fiți de acord cu termenii și condițiile', 'error');
                return;
            }
            
            // Validare număr de telefon cu intl-tel-input
            if (!window.iti) {
                showNotification('Eroare la inițializarea validării telefonului.', 'error');
                return;
            }
            
            // Validare mai flexibilă - verificăm doar că numărul are cel puțin 8 cifre
            const phoneDigits = phone.replace(/\D/g, '');
            if (phoneDigits.length < 8) {
                showNotification('Numărul de telefon trebuie să conțină cel puțin 8 cifre.', 'error');
                return;
            }
            
            // Verificăm dacă numărul este valid pentru țara selectată, dar nu blocăm dacă nu este
            if (!window.iti.isValidNumber()) {
                console.warn('Numărul de telefon nu este valid pentru țara selectată, dar continuăm cu înregistrarea.');
            }
            
            // Show loading state
            const submitBtn = document.getElementById('register-btn');
            
            // Prevent multiple submissions
            if (submitBtn.disabled) {
                return;
            }
            
            const originalText = submitBtn.innerHTML;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Se înregistrează...';
            submitBtn.disabled = true;
            
            // Create form data as URLSearchParams for string-based submission
            const formData = new URLSearchParams();
            formData.append('email', email);
            formData.append('password', password);
            formData.append('firstName', firstName);
            formData.append('lastName', lastName);
            formData.append('phone', phone);
            
            // Adăugăm prefixul țării din intl-tel-input
            if (window.iti) {
                const countryData = window.iti.getSelectedCountryData();
                const phonePrefix = '+' + countryData.dialCode;
                console.log('Selected country:', countryData.name);
                console.log('Phone prefix:', phonePrefix);
                formData.append('phonePrefix', phonePrefix);
            }
            
            // Upload image to Cloudinary first if image is selected
            let profileImageUrl = null;
            if (profileImage) {
                try {
                    const cloudinaryUrl = await uploadToCloudinary(profileImage);
                    profileImageUrl = cloudinaryUrl;
                } catch (error) {
                    console.error('Error uploading image:', error);
                    showNotification('Eroare la încărcarea imaginii. Încearcă din nou.', 'error');
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = originalText;
                    return;
                }
            }
            
            if (profileImageUrl) {
                formData.append('profileImageUrl', profileImageUrl);
            }
            
            console.log('FormData created with:');
            console.log('Email:', email);
            console.log('FirstName:', firstName);
            console.log('LastName:', lastName);
            console.log('Phone:', phone);
            if (window.iti) {
                const countryData = window.iti.getSelectedCountryData();
                console.log('PhonePrefix:', '+' + countryData.dialCode);
            }
            console.log('ProfileImage:', profileImage ? profileImage.name : 'null');
            console.log('ProfileImageUrl:', profileImageUrl);
            
            // reCAPTCHA v2 - get response
            if (typeof grecaptcha !== 'undefined') {
                const recaptchaResponse = grecaptcha.getResponse();
                if (recaptchaResponse) {
                    formData.append('recaptchaResponse', recaptchaResponse);
                    // Continue with form submission
                    submitRegistrationForm(formData, submitBtn, originalText);
                } else {
                    showNotification('Vă rugăm să completați reCAPTCHA.', 'error');
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = originalText;
                }
            } else {
                // If reCAPTCHA is not available, submit without it
                submitRegistrationForm(formData, submitBtn, originalText);
            }
        });
    }
});

// reCAPTCHA callback function
function onRecaptchaSuccess(token) {
    console.log('reCAPTCHA completed successfully');
}

// Submit registration form (for reCAPTCHA v3)
async function submitRegistrationForm(formData, submitBtn, originalText) {
    try {
        console.log('Sending registration request...');
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: formData
        });
        
        console.log('Registration response status:', response.status);
        console.log('Registration response headers:', response.headers);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
        if (data.success) {
            showNotification(data.message, 'success');
            // Reset reCAPTCHA
            if (typeof grecaptcha !== 'undefined') {
                grecaptcha.reset();
            }
            // Update navbar for logged in user
            if (typeof checkAuthStatus === 'function') {
                checkAuthStatus();
            }
            
            // Check for redirectAfterLogin in sessionStorage
            const redirectAfterLogin = sessionStorage.getItem('redirectAfterLogin');
            const floatingButtonRedirect = sessionStorage.getItem('floatingButtonRedirect');
            
            // Redirect after successful registration
            setTimeout(() => {
                console.log('🔄 Redirecting after successful registration...');
                console.log('📝 redirectAfterLogin from sessionStorage:', redirectAfterLogin);
                console.log('📝 floatingButtonRedirect from sessionStorage:', floatingButtonRedirect);
                
                let targetUrl = null;
                
                // Prioritate 1: redirectAfterLogin din sessionStorage (pentru butonul fluturător)
                if (redirectAfterLogin && floatingButtonRedirect) {
                    console.log('🎯 Using redirectAfterLogin from floating button:', redirectAfterLogin);
                    targetUrl = redirectAfterLogin;
                    sessionStorage.removeItem('redirectAfterLogin');
                    sessionStorage.removeItem('floatingButtonRedirect');
                }
                // Prioritate 2: redirectAfterLogin din sessionStorage (pentru navigare normală)
                else if (redirectAfterLogin) {
                    console.log('🎯 Using redirectAfterLogin from sessionStorage:', redirectAfterLogin);
                    targetUrl = redirectAfterLogin;
                    sessionStorage.removeItem('redirectAfterLogin');
                }
                // Prioritate 3: pagina principală
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
            showNotification(data.message, 'error');
            // Reset reCAPTCHA on error
            if (typeof grecaptcha !== 'undefined') {
                grecaptcha.reset();
            }
        }
    } catch (error) {
        console.error('Registration error details:', error);
        console.error('Error message:', error.message);
        console.error('Error stack:', error.stack);
        showNotification('A apărut o eroare. Încearcă din nou.', 'error');
    } finally {
        // Reset button state
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
    }
}

// Function to upload image to Cloudinary
async function uploadToCloudinary(file) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', 'rutex_profile_images'); // You'll need to create this preset in Cloudinary
    
    const response = await fetch('https://api.cloudinary.com/v1_1/de5efft4h/image/upload', {
        method: 'POST',
        body: formData
    });
    
    if (!response.ok) {
        throw new Error('Failed to upload image');
    }
    
    const data = await response.json();
    return data.secure_url;
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

/**
 * Inițializează input-ul pentru telefon cu intl-tel-input
 */
function initializePhoneInput() {
    const phoneInput = document.getElementById('phone');
    if (phoneInput && window.intlTelInput) {
        window.iti = window.intlTelInput(phoneInput, {
            initialCountry: 'md', // Moldova ca țară default
            preferredCountries: ['md', 'ro', 'ua', 'ru'], // țări preferate
            separateDialCode: true, // afișează codul de țară separat
            utilsScript: 'https://cdnjs.cloudflare.com/ajax/libs/intl-tel-input/17.0.8/js/utils.js',
            geoIpLookup: function(callback) {
                // Setăm Moldova ca default
                callback('md');
            },
            formatOnDisplay: true,
            autoHideDialCode: false,
            autoPlaceholder: 'aggressive'
        });
        
        // Eliminăm complet validarea în timp real pentru a evita mesajele de eroare
        // Validarea se va face doar la submit
    }
}
