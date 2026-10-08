function initializeDepartureTimeSelects() {
    const time = document.getElementById('departure-time');
    const group = document.querySelector('.ride-time-selects');
    if (!time || !group) return;
    const close = () => {
        group.querySelectorAll('.ride-time-options').forEach(panel => { panel.hidden = true; });
        group.querySelectorAll('.ride-time-toggle, [role="combobox"]').forEach(control => control.setAttribute('aria-expanded', 'false'));
    };
    const update = () => {
        const hour = document.getElementById('departure-hour').value;
        const minute = document.getElementById('departure-minute').value;
        time.value = /^(?:[01]\d|2[0-3])$/.test(hour) && /^[0-5]\d$/.test(minute) ? `${hour}:${minute}` : '';
        time.dispatchEvent(new Event('input', { bubbles: true }));
    };
    ['hour', 'minute'].forEach(kind => {
        const input = document.getElementById(`departure-${kind}`);
        const field = input.closest('.ride-time-field');
        const toggle = field.querySelector('.ride-time-toggle');
        const panel = field.querySelector('.ride-time-options');
        const limit = kind === 'hour' ? 24 : 60;
        for (let i = 0; i < limit; i++) {
            const option = document.createElement('button');
            option.type = 'button';
            option.textContent = String(i).padStart(2, '0');
            option.setAttribute('role', 'option');
            option.addEventListener('click', () => {
                input.value = option.textContent;
                update();
                close();
                input.focus({ preventScroll: true });
            });
            panel.appendChild(option);
        }
        toggle.addEventListener('click', () => {
            const opening = panel.hidden;
            close();
            if (!opening) return;
            panel.hidden = false;
            toggle.setAttribute('aria-expanded', 'true');
            input.setAttribute('aria-expanded', 'true');
            const options = [...panel.children];
            options.forEach(option => option.setAttribute('aria-selected', String(option.textContent === input.value)));
            const selected = options.find(option => option.textContent === input.value) || options[0];
            selected.focus({ preventScroll: true });
            panel.scrollTop = Math.max(0, selected.offsetTop - (panel.clientHeight - selected.offsetHeight) / 2);
        });
        input.addEventListener('click', () => toggle.click());
        input.addEventListener('keydown', event => {
            if (['ArrowDown', 'Enter', ' '].includes(event.key)) {
                event.preventDefault();
                toggle.click();
            }
        });
        panel.addEventListener('keydown', event => {
            if (event.key === 'Escape') { event.preventDefault(); close(); toggle.focus(); return; }
            const offsets = { ArrowDown: 1, ArrowUp: -1 };
            if (offsets[event.key] !== undefined) {
                event.preventDefault();
                const options = [...panel.children];
                const next = Math.max(0, Math.min(options.length - 1, options.indexOf(document.activeElement) + offsets[event.key]));
                options[next].focus();
            }
        });
    });
    document.addEventListener('click', event => { if (!group.contains(event.target)) close(); });
    document.querySelector('.add-ride-form').addEventListener('focusin', event => { if (!group.contains(event.target)) close(); });
    document.getElementById('flexible-time').addEventListener('change', close);
    update();
}

// Keep one section visible, while preserving values when navigating back.
function initializeProgressiveRideForm(editing = false) {
    const form = document.querySelector('.add-ride-form');
    if (!form) return;
    const ids = ['announcement-step', 'route-step', 'travel-step', 'publish-step'];
    let active = 0;
    let reached = editing ? ids.length - 1 : 0;
    const show = (index, focus = true) => {
        form.querySelectorAll('.ride-step-warning').forEach(warning => warning.remove());
        active = index;
        ids.forEach((id, i) => {
            const step = document.getElementById(id);
            step.hidden = i !== active;
            step.disabled = i !== active;
            const tab = form.querySelector(`[data-step-tab="${id}"]`);
            tab.disabled = i > reached;
            if (i === active) tab.setAttribute('aria-current', 'step');
            else tab.removeAttribute('aria-current');
        });
        if (focus) {
            const heading = document.getElementById(ids[active]).querySelector('.section-title');
            heading.tabIndex = -1;
            heading.focus({ preventScroll: true });
            form.scrollIntoView({ behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth', block: 'start' });
        }
    };
    const updateAnnouncement = () => {
        const passenger = isPassengerRequest();
        const selected = form.querySelector('input[name="announcementType"]:checked');
        const transport = document.getElementById('transport-step');
        transport.hidden = !selected || passenger;
        transport.disabled = !selected || passenger;
        const seats = document.getElementById('available-seats');
        if (passenger) {
            document.getElementById('seats-group').style.display = 'block';
            seats.disabled = false;
            seats.required = true;
        } else {
            const rideType = form.querySelector('input[name="rideType"]:checked');
            if (rideType) rideType.dispatchEvent(new Event('change', { bubbles: true }));
            if (document.getElementById('vehicle-select').value === '__new__') toggleVehicleForm(true);
        }
        // Changing the announcement requires checking the route and details again.
        reached = editing ? ids.length - 1 : 0;
        show(0, false);
    };
    const clearWarnings = () => form.querySelectorAll('.ride-step-warning').forEach(warning => warning.remove());
    const warn = (step, message) => {
        clearWarnings();
        const warning = document.createElement('p');
        warning.className = 'ride-step-warning';
        warning.setAttribute('role', 'alert');
        warning.tabIndex = -1;
        warning.textContent = message;
        step.querySelector('.step-actions, .form-actions').before(warning);
        warning.focus({ preventScroll: true });
    };
    form.addEventListener('input', clearWarnings);
    form.addEventListener('change', clearWarnings);
    const validateStep = index => {
        const step = document.getElementById(ids[index]);
        for (const input of step.querySelectorAll('input, select, textarea')) {
            if (input.required && input.type === 'text' && !input.value.trim()) input.value = '';
            if (input.willValidate && !input.checkValidity()) {
                let message;
                if (input.name === 'announcementType') {
                    message = rideFormText('Alege ce dorești să publici pentru a continua.', 'Выберите, что хотите опубликовать, чтобы продолжить.');
                } else if (input.name === 'rideType') {
                    message = rideFormText('Alege tipul de transport pentru a continua.', 'Выберите тип перевозки, чтобы продолжить.');
                } else if (input.id === 'terms') {
                    message = rideFormText('Acceptă Termenii și Condițiile pentru a publica anunțul.', 'Примите Условия использования, чтобы опубликовать объявление.');
                } else {
                    const label = form.querySelector(`label[for="${input.id}"]`)?.textContent.trim().replace(/:$/, '');
                    message = rideFormText(`Completează corect câmpul „${label || input.placeholder || input.name}” pentru a continua.`, `Заполните правильно поле «${label || input.placeholder || input.name}», чтобы продолжить.`);
                }
                warn(step, message);
                return false;
            }
        }
        if (index === 2 && !isPassengerRequest() && document.getElementById('vehicle-select').value === '__new__') {
            const missing = ['vehicle-make', 'vehicle-color', 'vehicle-plate']
                .map(id => document.getElementById(id)).find(input => !input.value.trim());
            if (missing) {
                warn(step, getVehicleText('fillError', 'Completați marca, culoarea și numărul mașinii.'));
                return false;
            }
        }
        return true;
    };
    const advance = () => {
        if (!validateStep(active)) return;
        reached = Math.max(reached, active + 1);
        show(active + 1);
    };
    form.querySelectorAll('input[name="announcementType"]').forEach(radio => radio.addEventListener('change', updateAnnouncement));
    form.querySelectorAll('[data-next-step]').forEach(button => button.addEventListener('click', advance));
    form.querySelectorAll('[data-previous-step]').forEach(button => button.addEventListener('click', () => show(active - 1)));
    form.querySelectorAll('[data-step-tab]').forEach(tab => tab.addEventListener('click', () => {
        const target = ids.indexOf(tab.dataset.stepTab);
        if (target > active && !validateStep(active)) return;
        show(target);
    }));
    form.addEventListener('submit', event => {
        if (active !== ids.length - 1) {
            event.preventDefault();
            event.stopImmediatePropagation();
            advance();
            return;
        }
        // Validate each section visibly, then include all sections in FormData.
        for (let i = 0; i < ids.length; i++) {
            show(i, false);
            if (!validateStep(i)) {
                event.preventDefault();
                event.stopImmediatePropagation();
                return;
            }
        }
        ids.forEach(id => { document.getElementById(id).disabled = false; });
    }, true);
    updateAnnouncement();
    return { refresh: updateAnnouncement };
}

