document.addEventListener('DOMContentLoaded', function() {
    initializePhoneInput();
    enforcePhoneCompletionMode();
    loadCurrentUserData();
    initializeImageUpload();
    initializeFormValidation();
    initializeTranslations();
});

function isForcePhoneCompletionMode() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('forcePhone') === 'true';
}

function enforcePhoneCompletionMode() {
    const forcePhone = isForcePhoneCompletionMode();
    if (!forcePhone) {
        return;
    }

    const phoneInput = document.getElementById('phone');
    if (phoneInput) {
        phoneInput.required = true;
        phoneInput.placeholder = translateText('phoneRequiredPlaceholder');
        bindPhoneRequirementVisibility(phoneInput);
    }

    highlightPhoneRequirement();
    if (!isPhoneFieldFilled()) {
        focusPhoneField();
    }

    showNotification(translateText('phoneRequiredContinue'), 'error');
}

function loadCurrentUserData() {
    fetch('/api/auth/user')
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
                window.location.href = '/' + currentLang + '/login';
                return null;
            }
        })
        .then(user => {
            if (user) {
                populateFormWithUserData(user);
                if (isForcePhoneCompletionMode()) {
                    highlightPhoneRequirement();
                    if (!isPhoneFieldFilled()) {
                        focusPhoneField();
                    }
                }
            }
        })
        .catch(error => {
            console.error('Error loading user data:', error);
            showNotification(translateText('userDataError'), 'error');
        });
}

function populateFormWithUserData(user) {
    document.getElementById('firstName').value = user.firstName || '';
    document.getElementById('lastName').value = user.lastName || '';
    document.getElementById('email').value = user.email || '';
    const phoneInput = document.getElementById('phone');
    
    // Setăm numărul de telefon după ce intl-tel-input este inițializat
    if (user.phone && window.iti) {
        window.iti.setNumber(user.phone);
    } else if (phoneInput) {
        phoneInput.value = user.phone || '';
    }
    
    // Handle profile image
    const currentProfileImage = document.getElementById('current-profile-image');
    const currentDefaultAvatar = document.getElementById('current-default-avatar');
    
    if (user.profileImage) {
        // Check if it's a Cloudinary URL (starts with http/https) or local file
        if (user.profileImage.startsWith('http://') || user.profileImage.startsWith('https://')) {
            // It's a Cloudinary URL, use it directly
            currentProfileImage.src = user.profileImage;
        } else {
            // It's a local file, use the old path
            currentProfileImage.src = `/uploads/profile-images/${user.profileImage}`;
        }
        currentProfileImage.style.display = 'block';
        currentDefaultAvatar.style.display = 'none';
        
        // Show remove image button
        document.getElementById('remove-image-btn').style.display = 'inline-flex';
    } else {
        currentProfileImage.style.display = 'none';
        currentDefaultAvatar.style.display = 'block';
    }
}

function highlightPhoneRequirement() {
    const phoneInput = document.getElementById('phone');
    if (!phoneInput) {
        return;
    }

    const phoneFormGroup = phoneInput.closest('.form-group');
    if (phoneFormGroup) {
        phoneFormGroup.classList.remove('valid');
        phoneFormGroup.classList.add('invalid');
    }

    phoneInput.setAttribute('aria-invalid', 'true');

    if (!document.getElementById('phone-required-warning')) {
        const warning = document.createElement('div');
        warning.id = 'phone-required-warning';
        warning.className = 'phone-required-warning';
        warning.innerHTML = '<i class="fas fa-exclamation-triangle"></i><span>' + translateText('phoneRequiredInline') + '</span>';

        const targetContainer = phoneInput.closest('.iti') || phoneInput;
        targetContainer.insertAdjacentElement('afterend', warning);
    }

    updatePhoneRequirementVisualState();
}

function bindPhoneRequirementVisibility(phoneInput) {
    if (!phoneInput || phoneInput.dataset.phoneRequirementBound === 'true') {
        return;
    }

    const updateState = () => updatePhoneRequirementVisualState();
    phoneInput.addEventListener('input', updateState);
    phoneInput.addEventListener('change', updateState);
    phoneInput.dataset.phoneRequirementBound = 'true';
}

function isPhoneFieldFilled() {
    const phoneInput = document.getElementById('phone');
    if (!phoneInput) {
        return false;
    }

    return phoneInput.value.replace(/\D/g, '').length > 0;
}

function updatePhoneRequirementVisualState() {
    const phoneInput = document.getElementById('phone');
    if (!phoneInput) {
        return;
    }

    const warning = document.getElementById('phone-required-warning');
    const phoneFormGroup = phoneInput.closest('.form-group');
    const hasValue = isPhoneFieldFilled();

    if (phoneFormGroup) {
        phoneFormGroup.classList.toggle('invalid', !hasValue);
        phoneFormGroup.classList.remove('valid');
    }

    phoneInput.setAttribute('aria-invalid', hasValue ? 'false' : 'true');

    if (warning) {
        const warningText = warning.querySelector('span');
        if (warningText) {
            warningText.textContent = translateText('phoneRequiredInline');
        }
        warning.style.display = hasValue ? 'none' : 'flex';
    }
}

function focusPhoneField() {
    const phoneInput = document.getElementById('phone');
    if (!phoneInput) {
        return;
    }

    setTimeout(() => {
        phoneInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
        phoneInput.focus();
    }, 150);
}

function initializeImageUpload() {
    const imageInput = document.getElementById('profile-image-input');
    const currentProfileImage = document.getElementById('current-profile-image');
    const currentDefaultAvatar = document.getElementById('current-default-avatar');
    const removeImageBtn = document.getElementById('remove-image-btn');
    
    imageInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            // Validate file type
            if (!file.type.startsWith('image/')) {
                showNotification(translateText('imageTypeError'), 'error');
                return;
            }
            
            // Validate file size (max 5MB)
            if (file.size > 5 * 1024 * 1024) {
                showNotification(translateText('imageSizeError'), 'error');
                return;
            }
            
            // Preview image
            const reader = new FileReader();
            reader.onload = function(e) {
                currentProfileImage.src = e.target.result;
                currentProfileImage.style.display = 'block';
                currentDefaultAvatar.style.display = 'none';
                removeImageBtn.style.display = 'inline-flex';
            };
            reader.readAsDataURL(file);
        }
    });
    
    removeImageBtn.addEventListener('click', function() {
        imageInput.value = '';
        currentProfileImage.style.display = 'none';
        currentDefaultAvatar.style.display = 'block';
        removeImageBtn.style.display = 'none';
    });
}

function initializeFormValidation() {
    const form = document.getElementById('edit-profile-form');
    
    // Phone validation is now handled by intl-tel-input
    
    // Add real-time password validation
    const newPasswordField = document.getElementById('newPassword');
    const confirmPasswordField = document.getElementById('confirmPassword');
    const currentPasswordField = document.getElementById('currentPassword');
    
    if (newPasswordField) {
        newPasswordField.addEventListener('input', validatePasswordStrength);
        newPasswordField.addEventListener('blur', validatePasswordMatch);
    }
    
    if (confirmPasswordField) {
        confirmPasswordField.addEventListener('input', validatePasswordMatch);
    }
    
    if (currentPasswordField) {
        currentPasswordField.addEventListener('input', clearPasswordValidation);
    }
    
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        if (validateForm()) {
            submitForm();
        }
    });
}

function validatePasswordStrength() {
    const newPassword = document.getElementById('newPassword');
    const confirmPassword = document.getElementById('confirmPassword');
    const passwordFeedback = getOrCreatePasswordFeedback();
    
    const password = newPassword.value;
    
    // Clear previous feedback
    passwordFeedback.innerHTML = '';
    passwordFeedback.className = 'password-feedback';
    
    if (password.length === 0) {
        // No password entered, hide feedback
        passwordFeedback.style.display = 'none';
        return;
    }
    
    passwordFeedback.style.display = 'block';
    
    // Check password strength
    const minLength = password.length >= 6;
    const hasLetter = /[a-zA-Z]/.test(password);
    const hasNumber = /\d/.test(password);
    
    let strength = 0;
    let feedbackItems = [];
    
    if (minLength) {
        strength++;
        feedbackItems.push('<span class="valid"><i class="fas fa-check"></i> ' + translateText('passwordStrength.minLength') + '</span>');
    } else {
        feedbackItems.push('<span class="invalid"><i class="fas fa-times"></i> ' + translateText('passwordStrength.minLength') + '</span>');
    }
    
    if (hasLetter) {
        strength++;
        feedbackItems.push('<span class="valid"><i class="fas fa-check"></i> ' + translateText('passwordStrength.hasLetters') + '</span>');
    } else {
        feedbackItems.push('<span class="invalid"><i class="fas fa-times"></i> ' + translateText('passwordStrength.hasLetters') + '</span>');
    }
    
    if (hasNumber) {
        strength++;
        feedbackItems.push('<span class="valid"><i class="fas fa-check"></i> ' + translateText('passwordStrength.hasNumbers') + '</span>');
    } else {
        feedbackItems.push('<span class="invalid"><i class="fas fa-times"></i> ' + translateText('passwordStrength.hasNumbers') + '</span>');
    }
    
    // Set strength class
    if (strength === 3) {
        passwordFeedback.classList.add('strong');
    } else if (strength >= 2) {
        passwordFeedback.classList.add('medium');
    } else {
        passwordFeedback.classList.add('weak');
    }
    
    passwordFeedback.innerHTML = feedbackItems.join('<br>');
    
    // Also check password match if confirm password is filled
    if (confirmPassword.value) {
        validatePasswordMatch();
    }
}

function validatePasswordMatch() {
    const newPassword = document.getElementById('newPassword');
    const confirmPassword = document.getElementById('confirmPassword');
    const matchFeedback = getOrCreateMatchFeedback();
    
    const password = newPassword.value;
    const confirm = confirmPassword.value;
    
    // Clear previous feedback
    matchFeedback.innerHTML = '';
    matchFeedback.className = 'password-match-feedback';
    
    if (confirm.length === 0) {
        // No confirmation password entered, hide feedback
        matchFeedback.style.display = 'none';
        return;
    }
    
    matchFeedback.style.display = 'block';
    
    if (password === confirm) {
        matchFeedback.classList.add('match');
        matchFeedback.innerHTML = '<span class="valid"><i class="fas fa-check"></i> ' + translateText('passwordStrength.passwordsMatch') + '</span>';
    } else {
        matchFeedback.classList.add('no-match');
        matchFeedback.innerHTML = '<span class="invalid"><i class="fas fa-times"></i> ' + translateText('passwordStrength.passwordsDontMatch') + '</span>';
    }
}

function clearPasswordValidation() {
    const passwordFeedback = document.getElementById('password-feedback');
    const matchFeedback = document.getElementById('password-match-feedback');
    
    if (passwordFeedback) {
        passwordFeedback.style.display = 'none';
    }
    
    if (matchFeedback) {
        matchFeedback.style.display = 'none';
    }
}

function getOrCreatePasswordFeedback() {
    let feedback = document.getElementById('password-feedback');
    if (!feedback) {
        feedback = document.createElement('div');
        feedback.id = 'password-feedback';
        feedback.className = 'password-feedback';
        
        const newPasswordField = document.getElementById('newPassword');
        newPasswordField.parentNode.appendChild(feedback);
    }
    return feedback;
}

function getOrCreateMatchFeedback() {
    let feedback = document.getElementById('password-match-feedback');
    if (!feedback) {
        feedback = document.createElement('div');
        feedback.id = 'password-match-feedback';
        feedback.className = 'password-match-feedback';
        
        const confirmPasswordField = document.getElementById('confirmPassword');
        confirmPasswordField.parentNode.appendChild(feedback);
    }
    return feedback;
}

function validateForm() {
    const firstName = document.getElementById('firstName').value.trim();
    const lastName = document.getElementById('lastName').value.trim();
    const email = document.getElementById('email').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const forcePhone = isForcePhoneCompletionMode();
    
    // Validate required fields
    if (!firstName || !lastName || !email) {
        showNotification(translateText('requiredFields'), 'error');
        return false;
    }
    
    // Validate email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        showNotification(translateText('invalidEmail'), 'error');
        return false;
    }
    
    // Validate phone number with intl-tel-input
    if (forcePhone && !phone) {
        showNotification(translateText('phoneRequiredPlaceholder'), 'error');
        return false;
    }

    if (phone) {
        if (!window.iti) {
            showNotification(translateText('phoneValidationInitError'), 'error');
            return false;
        }
        
        // Validare mai flexibilă - verificăm doar că numărul are cel puțin 8 cifre
        const phoneDigits = phone.replace(/\D/g, '');
        if (phoneDigits.length < 8) {
            showNotification(translateText('phoneMinDigitsError'), 'error');
            return false;
        }
        
        // Verificăm dacă numărul este valid pentru țara selectată, dar nu blocăm dacă nu este
        if (!window.iti.isValidNumber()) {
            console.warn('Numărul de telefon nu este valid pentru țara selectată, dar continuăm cu actualizarea.');
        }
    }
    
    return true;
}



async function submitForm() {
    const form = document.getElementById('edit-profile-form');
    
    // Get form data as regular form fields
    const formData = new URLSearchParams();
    formData.append('firstName', document.getElementById('firstName').value);
    formData.append('lastName', document.getElementById('lastName').value);
    formData.append('email', document.getElementById('email').value);
    formData.append('phone', document.getElementById('phone').value);
    
    // Adăugăm prefixul țării din intl-tel-input
    if (window.iti) {
        const countryData = window.iti.getSelectedCountryData();
        const phonePrefix = '+' + countryData.dialCode;
        console.log('Selected country:', countryData.name);
        console.log('Phone prefix:', phonePrefix);
        formData.append('phonePrefix', phonePrefix);
    }
    
    // Upload image to Cloudinary first if image is selected
    const profileImageInput = document.getElementById('profile-image-input');
    if (profileImageInput && profileImageInput.files[0]) {
        try {
            const cloudinaryUrl = await uploadToCloudinary(profileImageInput.files[0]);
            formData.append('profileImageUrl', cloudinaryUrl);
        } catch (error) {
            console.error('Error uploading image:', error);
            showNotification(translateText('imageUploadError'), 'error');
            return;
        }
    }
    
    // Show loading state
    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> ' + translateText('saving');
    submitBtn.disabled = true;
    
    fetch('/api/users/update-profile', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showNotification(translateText('profileUpdated'), 'success');
            setTimeout(() => {
                const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
                window.location.href = '/' + currentLang + '/profile';
            }, 1500);
        } else {
            showNotification(data.message || translateText('profileUpdateError'), 'error');
        }
    })
    .catch(error => {
        console.error('Error updating profile:', error);
        showNotification(translateText('generalError'), 'error');
    })
    .finally(() => {
        // Restore button state
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
    });
}

function showNotification(message, type) {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerHTML = `
        <div class="notification-content ${type}">
            <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
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

// Translation object for edit-profile page
const translations = {
    'ro': {
        'pageTitle': 'Editează Profilul',
        'pageSubtitle': 'Actualizează informațiile tale personale',
        'profileImage': 'Imagine de profil',
        'uploadImage': 'Încarcă imagine',
        'removeImage': 'Șterge imaginea',
        'personalInfo': 'Informații personale',
        'firstName': 'Prenume *',
        'lastName': 'Nume *',
        'email': 'Email *',
        'phone': 'Telefon',
        'phonePlaceholder': '+40 7XX XXX XXX',
        'phoneRequiredPlaceholder': 'Numărul de telefon este obligatoriu pentru finalizarea contului',
        'phoneRequiredContinue': 'Te rugăm să adaugi numărul de telefon pentru a continua.',
        'phoneRequiredInline': 'Numărul de telefon este obligatoriu pentru activarea completă a contului.',
        'phoneValidationInitError': 'Eroare la inițializarea validării telefonului.',
        'phoneMinDigitsError': 'Numărul de telefon trebuie să conțină cel puțin 8 cifre.',
        'changePassword': 'Schimbă parola',
        'showPasswordSection': 'Schimbă parola',
        'hidePasswordSection': 'Ascunde',
        'passwordDescription': 'Completează aceste câmpuri doar dacă vrei să schimbi parola. Dacă nu completezi nimic, parola rămâne neschimbată.',
        'currentPassword': 'Parola actuală',
        'currentPasswordPlaceholder': 'Introdu parola actuală',
        'newPassword': 'Parola nouă',
        'newPasswordPlaceholder': 'Introdu parola nouă',
        'confirmPassword': 'Confirmă parola nouă',
        'confirmPasswordPlaceholder': 'Confirmă parola nouă',
        'cancel': 'Anulează',
        'saveChanges': 'Salvează modificările',
        'saving': 'Se salvează...',
        'profileUpdated': 'Profilul a fost actualizat cu succes!',
        'profileUpdateError': 'Eroare la actualizarea profilului',
        'generalError': 'A apărut o eroare la actualizarea profilului',
        'requiredFields': 'Te rog completează toate câmpurile obligatorii',
        'invalidEmail': 'Te rog introdu o adresă de email validă',
        'currentPasswordRequired': 'Te rog introdu parola actuală pentru a schimba parola',
        'confirmPasswordRequired': 'Te rog confirmă parola nouă',
        'passwordMinLength': 'Parola nouă trebuie să aibă cel puțin 6 caractere',
        'passwordsDontMatch': 'Parolele nu se potrivesc',
        'imageTypeError': 'Te rog selectează doar fișiere imagine',
        'imageSizeError': 'Imaginea este prea mare. Dimensiunea maximă este 5MB',
        'userDataError': 'Eroare la încărcarea datelor utilizatorului',
        'passwordStrength': {
            'minLength': 'Cel puțin 6 caractere',
            'hasLetters': 'Conține litere',
            'hasNumbers': 'Conține numere',
            'passwordsMatch': 'Parolele se potrivesc',
            'passwordsDontMatch': 'Parolele nu se potrivesc'
        }
    },
    'ru': {
        'pageTitle': 'Редактировать Профиль',
        'pageSubtitle': 'Обновите вашу личную информацию',
        'profileImage': 'Фото профиля',
        'uploadImage': 'Загрузить фото',
        'removeImage': 'Удалить фото',
        'personalInfo': 'Личная информация',
        'firstName': 'Имя *',
        'lastName': 'Фамилия *',
        'email': 'Email *',
        'phone': 'Телефон',
        'phonePlaceholder': '+40 7XX XXX XXX',
        'phoneRequiredPlaceholder': 'Номер телефона обязателен для завершения аккаунта',
        'phoneRequiredContinue': 'Пожалуйста, добавьте номер телефона, чтобы продолжить.',
        'phoneRequiredInline': 'Номер телефона обязателен для полной активации аккаунта.',
        'phoneValidationInitError': 'Ошибка инициализации проверки телефона.',
        'phoneMinDigitsError': 'Номер телефона должен содержать минимум 8 цифр.',
        'changePassword': 'Изменить пароль',
        'showPasswordSection': 'Изменить пароль',
        'hidePasswordSection': 'Скрыть',
        'passwordDescription': 'Заполните эти поля только если хотите изменить пароль. Если ничего не заполните, пароль останется неизменным.',
        'currentPassword': 'Текущий пароль',
        'currentPasswordPlaceholder': 'Введите текущий пароль',
        'newPassword': 'Новый пароль',
        'newPasswordPlaceholder': 'Введите новый пароль',
        'confirmPassword': 'Подтвердите новый пароль',
        'confirmPasswordPlaceholder': 'Подтвердите новый пароль',
        'cancel': 'Отмена',
        'saveChanges': 'Сохранить изменения',
        'saving': 'Сохранение...',
        'profileUpdated': 'Профиль успешно обновлен!',
        'profileUpdateError': 'Ошибка при обновлении профиля',
        'generalError': 'Произошла ошибка при обновлении профиля',
        'requiredFields': 'Пожалуйста, заполните все обязательные поля',
        'invalidEmail': 'Пожалуйста, введите действительный email',
        'currentPasswordRequired': 'Пожалуйста, введите текущий пароль для изменения',
        'confirmPasswordRequired': 'Пожалуйста, подтвердите новый пароль',
        'passwordMinLength': 'Новый пароль должен содержать минимум 6 символов',
        'passwordsDontMatch': 'Пароли не совпадают',
        'imageTypeError': 'Пожалуйста, выберите только файлы изображений',
        'imageSizeError': 'Изображение слишком большое. Максимальный размер 5MB',
        'userDataError': 'Ошибка при загрузке данных пользователя',
        'passwordStrength': {
            'minLength': 'Минимум 6 символов',
            'hasLetters': 'Содержит буквы',
            'hasNumbers': 'Содержит цифры',
            'passwordsMatch': 'Пароли совпадают',
            'passwordsDontMatch': 'Пароли не совпадают'
        }
    }
};

function getCurrentLanguage() {
    const currentLangElement = document.querySelector('.current-lang');
    return currentLangElement ? (currentLangElement.textContent === 'RO' ? 'ro' : 'ru') : 'ro';
}

function translateText(key) {
    const lang = getCurrentLanguage();
    const translation = translations[lang];
    
    if (key.includes('.')) {
        const keys = key.split('.');
        let value = translation;
        for (const k of keys) {
            value = value[k];
            if (!value) break;
        }
        return value || key;
    }
    
    return translation[key] || key;
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

function initializeTranslations() {
    const lang = getCurrentLanguage();
    
    // Update page title
    document.title = translateText('pageTitle') + ' - Rutex';
    
    // Update main page elements
    const pageTitle = document.querySelector('.header-text h1');
    if (pageTitle) pageTitle.textContent = translateText('pageTitle');
    
    const pageSubtitle = document.querySelector('.header-text p');
    if (pageSubtitle) pageSubtitle.textContent = translateText('pageSubtitle');
    
    // Update form sections
    const profileImageTitle = document.querySelector('.form-section:nth-child(1) .section-header h3');
    if (profileImageTitle) profileImageTitle.textContent = translateText('profileImage');
    
    const personalInfoTitle = document.querySelector('.form-section:nth-child(2) .section-header h3');
    if (personalInfoTitle) personalInfoTitle.textContent = translateText('personalInfo');
    
    // Update form labels and placeholders
    const firstNameLabel = document.querySelector('label[for="firstName"]');
    if (firstNameLabel) firstNameLabel.textContent = translateText('firstName');
    
    const lastNameLabel = document.querySelector('label[for="lastName"]');
    if (lastNameLabel) lastNameLabel.textContent = translateText('lastName');
    
    const emailLabel = document.querySelector('label[for="email"]');
    if (emailLabel) emailLabel.textContent = translateText('email');
    
    const phoneLabel = document.querySelector('label[for="phone"]');
    if (phoneLabel) phoneLabel.textContent = translateText('phone');
    
    const phoneInput = document.getElementById('phone');
    if (phoneInput) {
        phoneInput.placeholder = isForcePhoneCompletionMode()
            ? translateText('phoneRequiredPlaceholder')
            : translateText('phonePlaceholder');
    }
    
    // Update buttons
    const uploadButton = document.querySelector('.btn-upload span');
    if (uploadButton) uploadButton.textContent = translateText('uploadImage');
    
    const removeButton = document.querySelector('.btn-remove span');
    if (removeButton) removeButton.textContent = translateText('removeImage');
    
    const cancelButton = document.querySelector('.btn-cancel span');
    if (cancelButton) cancelButton.textContent = translateText('cancel');
    
    const saveButton = document.querySelector('.btn-save span');
    if (saveButton) saveButton.textContent = translateText('saveChanges');
}

/**
 * Inițializează input-ul pentru telefon cu intl-tel-input
 */
function initializePhoneInput() {
    const phoneInput = document.getElementById('phone');
    if (phoneInput) {
        phoneInput.setAttribute('autocomplete', 'new-password');
        phoneInput.setAttribute('autocorrect', 'off');
        phoneInput.setAttribute('autocapitalize', 'none');
        phoneInput.setAttribute('spellcheck', 'false');
        phoneInput.setAttribute('name', 'profile_phone_input');

        const phoneFormGroup = phoneInput.closest('.form-group');
        if (phoneFormGroup) {
            phoneFormGroup.classList.add('phone-validation-group');
        }

        phoneInput.addEventListener('input', function() {
            const digitsOnly = this.value.replace(/\D/g, '');
            if (this.value !== digitsOnly) {
                this.value = digitsOnly;
            }
        });
    }

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
            formatOnDisplay: false,
            autoHideDialCode: false,
            autoPlaceholder: 'aggressive'
        });

        phoneInput.setAttribute('autocomplete', 'new-password');
        phoneInput.setAttribute('autocorrect', 'off');
        phoneInput.setAttribute('autocapitalize', 'none');
        phoneInput.setAttribute('spellcheck', 'false');
        phoneInput.setAttribute('name', 'profile_phone_input');
        
        // Eliminăm complet validarea în timp real pentru a evita mesajele de eroare
        // Validarea se va face doar la submit
    }
}
