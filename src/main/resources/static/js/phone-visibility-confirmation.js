let phoneVisibilityConfirmationOpen = false;

function confirmPhoneVisibility(showPhoneNumber) {
    if (showPhoneNumber) return Promise.resolve(true);
    if (phoneVisibilityConfirmationOpen) return Promise.resolve(false);
    phoneVisibilityConfirmationOpen = true;

    const russian = document.documentElement.lang === 'ru';
    const dialog = document.createElement('dialog');
    dialog.className = 'phone-visibility-dialog';
    dialog.setAttribute('aria-labelledby', 'phone-visibility-title');
    dialog.setAttribute('aria-describedby', 'phone-visibility-description');

    const title = document.createElement('h3');
    title.id = 'phone-visibility-title';
    title.textContent = russian ? 'Продолжить без номера телефона?' : 'Continui fără afișarea numărului de telefon?';
    const description = document.createElement('p');
    description.id = 'phone-visibility-description';
    description.textContent = russian
        ? 'Вы уверены, что не хотите показывать номер телефона в этом объявлении? С номером телефона другие пользователи смогут связаться с вами напрямую.'
        : 'Ești sigur că nu dorești să afișezi numărul de telefon în acest anunț? Cu un număr de telefon, ceilalți utilizatori te pot contacta direct.';
    const actions = document.createElement('div');
    actions.className = 'phone-visibility-actions';
    const back = document.createElement('button');
    back.type = 'button';
    back.className = 'btn phone-visibility-back';
    back.textContent = russian ? 'Вернуться к выбору' : 'Revin la bifă';
    const proceed = document.createElement('button');
    proceed.type = 'button';
    proceed.className = 'btn phone-visibility-continue';
    proceed.textContent = russian ? 'Продолжить без номера' : 'Continui fără telefon';
    actions.append(back, proceed);
    dialog.append(title, description, actions);
    document.body.append(dialog);

    return new Promise(resolve => {
        function finish(confirmed) {
            dialog.close();
            dialog.remove();
            phoneVisibilityConfirmationOpen = false;
            if (!confirmed) document.getElementById('show-phone-number')?.focus();
            resolve(confirmed);
        }
        back.addEventListener('click', () => finish(false));
        proceed.addEventListener('click', () => finish(true));
        dialog.addEventListener('click', event => {
            const bounds = dialog.getBoundingClientRect();
            if (event.target === dialog && (event.clientX < bounds.left || event.clientX > bounds.right ||
                event.clientY < bounds.top || event.clientY > bounds.bottom)) {
                finish(false);
            }
        });
        dialog.addEventListener('cancel', event => {
            event.preventDefault();
            finish(false);
        });
        dialog.showModal();
    });
}

let inlinePhoneCheck = async () => true;

async function preparePhoneVisibility(formData) {
    if (formData.get('showPhoneNumber') !== 'true') return true;
    if (!await inlinePhoneCheck()) return false;
    const input = document.getElementById('contact-phone');
    if (input && !input.disabled) {
        const dialCode = window.iti?.getSelectedCountryData().dialCode;
        formData.set('contactPhone', dialCode ? `+${dialCode}${input.value.replace(/\D/g, '').replace(/^0/, '')}` : input.value.trim());
    }
    return true;
}

document.addEventListener('DOMContentLoaded', () => {
    const checkbox = document.getElementById('show-phone-number');
    const fields = document.getElementById('inline-phone-fields');
    const input = document.getElementById('contact-phone');
    if (!checkbox || !fields || !input) return;
    initializeProfilePhoneInput(input, 'contactPhone');
    input.disabled = true;
    const russian = document.documentElement.lang === 'ru';
    let revision = 0;
    async function update() {
        const currentRevision = ++revision;
        if (!checkbox.checked) {
            fields.hidden = true;
            input.disabled = true;
            input.required = false;
            input.removeAttribute('aria-invalid');
            return true;
        }
        try {
            const response = await fetch('/api/auth/user');
            if (!response.ok) return false;
            const user = await response.json();
            if (revision !== currentRevision) return false;
            const otherOwner = checkbox.dataset.ownerId && checkbox.dataset.ownerId !== String(user.id);
            const missing = !otherOwner && String(user.phone || '').replace(/\D/g, '').length < 8;
            fields.hidden = !missing;
            input.disabled = !missing;
            input.required = missing;
            return true;
        } catch (error) {
            return false;
        }
    }
    input.addEventListener('invalid', () => input.setAttribute('aria-invalid', 'true'));
    input.addEventListener('input', () => {
        input.setCustomValidity('');
        if (input.getAttribute('aria-invalid') === 'true') {
            const digits = input.value.replace(/\D/g, '');
            input.setAttribute('aria-invalid', String(digits.length < 8 || digits.length > 15));
        }
    });
    checkbox.addEventListener('change', update);
    inlinePhoneCheck = async () => {
        if (!await update()) return false;
        if (input.disabled) return true;
        const digits = input.value.replace(/\D/g, '');
        const valid = /^[+0-9\s().-]+$/.test(input.value) && digits.length >= 8 && digits.length <= 15;
        input.setCustomValidity(valid ? '' : (russian ? 'Введите корректный номер телефона (8–15 цифр).' : 'Introdu un număr de telefon valid (8–15 cifre).'));
        return input.reportValidity();
    };
    update();
});
