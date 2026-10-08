function initializeProfilePhoneInput(phoneInput, inputName = 'profile_phone_input') {
    if (phoneInput) {
        phoneInput.setAttribute('autocomplete', 'new-password');
        phoneInput.setAttribute('autocorrect', 'off');
        phoneInput.setAttribute('autocapitalize', 'none');
        phoneInput.setAttribute('spellcheck', 'false');
        phoneInput.setAttribute('name', inputName);

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
        phoneInput.setAttribute('name', inputName);
        
        // Eliminăm complet validarea în timp real pentru a evita mesajele de eroare
        // Validarea se va face doar la submit
    }
    return window.iti;
}
