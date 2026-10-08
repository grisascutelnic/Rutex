let map;
let isSubmittingRide = false;
let editFormSteps;
let loadedRide;

function getCurrentLang() {
    return document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
}

function setSubmitState(isSubmitting) {
    isSubmittingRide = isSubmitting;
    const submitBtn = document.querySelector('#edit-ride-form .btn.btn-primary[type="submit"]');
    const previewBtn = document.getElementById('preview-ride');
    const modalSubmitBtn = document.querySelector('#preview-modal .modal-footer .btn.btn-primary');

    [submitBtn, previewBtn, modalSubmitBtn].forEach(btn => {
        if (!btn) {
            return;
        }

        if (!btn.dataset.originalHtml) {
            btn.dataset.originalHtml = btn.innerHTML;
        }

        btn.disabled = isSubmitting;
        btn.classList.toggle('is-submitting', isSubmitting);
        btn.setAttribute('aria-busy', isSubmitting ? 'true' : 'false');

        if (btn === submitBtn) {
            btn.innerHTML = isSubmitting
                ? `<i class="fas fa-spinner fa-spin"></i> ${rideFormText('Se actualizează...', 'Сохранение...')}`
                : btn.dataset.originalHtml;
        }
    });
}

function slugifyRideLocation(value) {
    return String(value || '')
        .split(',')[0]
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .replace(/[^\p{L}\p{N}]+/gu, '-')
        .replace(/^-+|-+$/g, '');
}

function buildRideUrl(ride) {
    if (!ride || !ride.id) {
        return `/${getCurrentLang()}/rides`;
    }

    const fromSlug = slugifyRideLocation(ride.fromLocation);
    const toSlug = slugifyRideLocation(ride.toLocation);
    const routeSlug = `${fromSlug}-${toSlug}`.replace(/-+/g, '-').replace(/^-+|-+$/g, '') || 'ride';
    return `/${getCurrentLang()}/ride/${routeSlug}-${ride.id}`;
}

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('edit-ride-form');
    const rideId = getRideIdFromUrl();
    form.inert = true;
    form.setAttribute('aria-busy', 'true');
    initializeDepartureTimeSelects();
    editFormSteps = initializeProgressiveRideForm(true);
    initializeEditCalendar();
    document.getElementById('vehicle-select').addEventListener('change', () => {
        toggleVehicleForm(document.getElementById('vehicle-select').value === '__new__');
    });
    
    // Initialize translations
    const currentLang = getCurrentLang();
    updateEditRideTranslations(currentLang);
    
    // Initialize autocomplete
    try {
        initializeLocationAutocomplete();
        console.log('Location autocomplete initialized');
    } catch (error) {
        console.error('Error initializing autocomplete:', error);
    }
    
    if (!rideId) {
        showError(getEditRideTranslation('loadingError', currentLang));
        return;
    }
    
    // Încărcăm datele cursei pentru editare după ce DOM-ul este complet încărcat
    setTimeout(() => {
        loadRideData(rideId);
    }, 100);
    
    // Handler pentru submit
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        updateRide(rideId);
    });
    
    document.getElementById('flexible-time').addEventListener('change', updateFlexibleTimeInterface);

    document.querySelectorAll('input[name="rideType"], input[name="announcementType"]').forEach(radio => {
        radio.addEventListener('change', () => {
            editingPassengerRequest = document.querySelector('input[name="announcementType"]:checked').value === 'PASSENGER_REQUEST';
            updateRideTypeInterface();
        });
    });

    // Handler pentru butonul de previzualizare
    const previewBtn = document.getElementById('preview-ride');
    if (previewBtn) {
        previewBtn.addEventListener('click', showPreview);
        console.log('Preview button handler added');
    } else {
        console.warn('Preview button not found');
    }
});

function getRideIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('id');
}

function loadRideData(rideId) {
    console.log('Loading ride data for ID:', rideId);
    
    fetch(`/api/rides/${rideId}/edit`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        console.log('Response status:', response.status);
        console.log('Response ok:', response.ok);
        
        if (!response.ok) {
            if (response.status === 401) {
                window.location.href = `/${getCurrentLang()}/login`;
                return;
            }
            if (response.status === 403) {
                const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
                showError(getEditRideTranslation('permissionError', currentLang));
                return;
            }
            throw new Error('Eroare la încărcarea datelor cursei');
        }
        return response.json();
    })
    .then(data => {
        console.log('Received data:', data);
        
        if (!data) return;
        if (data.success) {
            console.log('Ride data to populate:', data.ride);
            loadedRide = data.ride;
            populateForm(data.ride);
            formReady();
        } else {
            const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
            showError(data.message || getEditRideTranslation('loadError', currentLang));
        }
    })
    .catch(error => {
        console.error('Error:', error);
        const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
        showError(getEditRideTranslation('loadError', currentLang));
    });
}

function formReady() {
    const form = document.getElementById('edit-ride-form');
    form.inert = false;
    form.setAttribute('aria-busy', 'false');
}

function populateForm(ride) {
    console.log('Populating form with ride data:', ride);
    
    try {
        // Populăm câmpurile cu datele existente
        const fromLocationElement = document.getElementById('from-location');
        const toLocationElement = document.getElementById('to-location');
        const travelDateElement = document.getElementById('travel-date');
        const departureTimeElement = document.getElementById('departure-time');
        const availableSeatsElement = document.getElementById('available-seats');
        const descriptionElement = document.getElementById('description');
        
        console.log('Found elements:', {
            fromLocation: !!fromLocationElement,
            toLocation: !!toLocationElement,
            travelDate: !!travelDateElement,
            departureTime: !!departureTimeElement,
            availableSeats: !!availableSeatsElement,
            description: !!descriptionElement
        });
        
        if (fromLocationElement) fromLocationElement.value = ride.fromLocation || '';
        if (toLocationElement) toLocationElement.value = ride.toLocation || '';
        
        // Păstrăm exact data cursei, fără conversie de fus orar.
        if (ride.travelDate && travelDateElement) {
            console.log('Original travel date:', ride.travelDate);
            const formattedDate = formatRideDateForInput(ride.travelDate);
            console.log('Formatted date:', formattedDate);
            if (travelDateElement._flatpickr) travelDateElement._flatpickr.setDate(formattedDate, false, 'Y-m-d');
            else travelDateElement.value = formattedDate;
        }
        
        // Păstrăm ora existentă din cursă.
        if (ride.departureTime && departureTimeElement) {
            console.log('Original departure time:', ride.departureTime);
            const formattedTime = formatRideTimeForInput(ride.departureTime);
            console.log('Formatted time:', formattedTime);
            departureTimeElement.value = formattedTime;
            document.getElementById('departure-hour').value = formattedTime.slice(0, 2);
            document.getElementById('departure-minute').value = formattedTime.slice(3, 5);
        }
        
        document.getElementById('flexible-time').checked = ride.flexibleTime === true;
        updateFlexibleTimeInterface();
        editingPassengerRequest = ride.announcementType === 'PASSENGER_REQUEST';
        if (availableSeatsElement) availableSeatsElement.value = editingPassengerRequest ? (ride.requestedSeats || 1) : (ride.availableSeats || '');
        const seatsLabel = document.querySelector('label[for="available-seats"]');
        if (seatsLabel && editingPassengerRequest) seatsLabel.textContent = rideFormText("Număr de pasageri:", "Количество пассажиров:");
        const vehicleGroup = document.getElementById('vehicle-group');
        const vehicleSelect = document.getElementById('vehicle-select');
        if (vehicleGroup) vehicleGroup.hidden = editingPassengerRequest;
        if (vehicleSelect) vehicleSelect.required = !editingPassengerRequest;
        if (descriptionElement) descriptionElement.value = ride.description || '';
        const phoneConsent = document.getElementById('show-phone-number');
        phoneConsent.dataset.ownerId = String(ride.userId);
        phoneConsent.checked = ride.showPhoneNumber === true;
        phoneConsent.dispatchEvent(new Event('change'));
        loadEditVehicles(ride);
        
        document.querySelector(`input[name="announcementType"][value="${editingPassengerRequest ? 'PASSENGER_REQUEST' : 'DRIVER_OFFER'}"]`).checked = true;
        const selectedType = ride.isPackageOnly ? 'packages-only' : (ride.transportAndPackages ? 'passengers-and-packages' : 'passengers-only');
        document.querySelector(`input[name="rideType"][value="${selectedType}"]`).checked = true;
        updateRideTypeInterface();
        editFormSteps.refresh();

        console.log('Form populated successfully');
    } catch (error) {
        console.error('Error populating form:', error);
        throw error;
    }
}

function formatRideDateForInput(value) {
    if (Array.isArray(value) && value.length >= 3) {
        return `${value[0]}-${String(value[1]).padStart(2, '0')}-${String(value[2]).padStart(2, '0')}`;
    }
    const match = String(value).match(/^(\d{4}-\d{2}-\d{2})/);
    return match ? match[1] : '';
}

function formatRideTimeForInput(value) {
    if (Array.isArray(value) && value.length >= 5) {
        return `${String(value[3]).padStart(2, '0')}:${String(value[4]).padStart(2, '0')}`;
    }
    const match = String(value).match(/(?:T|^)(\d{2}:\d{2})/);
    return match ? match[1] : '';
}

function isPassengerRequest() {
    return document.querySelector('input[name="announcementType"]:checked')?.value === 'PASSENGER_REQUEST';
}

function getVehicleText(key, fallback) {
    return document.getElementById('vehicle-select').dataset[key] || fallback;
}

function toggleVehicleForm(show) {
    document.getElementById('vehicle-form').style.display = show ? 'block' : 'none';
}

async function loadEditVehicles(ride) {
    const select = document.getElementById('vehicle-select');
    select.replaceChildren(new Option(getVehicleText('placeholder', ''), ''));
    // Always retain the saved vehicle/snapshot, even if it was removed from the profile
    // or a moderator is editing an announcement owned by another user.
    if (ride.vehicleId != null || ride.vehicleMake) {
        const value = ride.vehicleId != null ? String(ride.vehicleId) : '__existing__';
        select.add(new Option([ride.vehicleMake, ride.vehiclePlateNumber].filter(Boolean).join(' · '), value));
        select.value = value;
    }
    try {
        const response = await fetch('/api/vehicles');
        if (!response.ok) throw new Error(rideFormText('Vehiculele nu au putut fi încărcate.', 'Не удалось загрузить автомобили.'));
        const vehicles = await response.json();
        vehicles.forEach(vehicle => {
            if (String(vehicle.id) !== String(ride.vehicleId)) {
                select.add(new Option(`${vehicle.make} · ${vehicle.plateNumber}`, String(vehicle.id)));
            }
        });
        select.add(new Option(getVehicleText('addNew', '+ Adaugă vehicul nou'), '__new__'));
    } catch (error) {
        showError(error.message);
    }
}

function initializeEditCalendar() {
    if (typeof flatpickr === 'undefined') return;
    flatpickr(document.getElementById('travel-date'), {
        dateFormat: 'Y-m-d',
        altInput: true,
        altFormat: 'd/m/Y',
        locale: document.documentElement.lang === 'ru' ? 'ru' : 'ro',
        disableMobile: true,
        allowInput: false
    });
}

async function createEditVehicle() {
    const make = document.getElementById('vehicle-make').value.trim();
    const color = document.getElementById('vehicle-color').value.trim();
    const plateNumber = document.getElementById('vehicle-plate').value.trim().toUpperCase();
    if (!make || !color || !plateNumber) throw new Error(getVehicleText('fillError', 'Completează datele vehiculului.'));
    const response = await fetch('/api/vehicles', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ make, color, plateNumber })
    });
    const data = await response.json();
    if (!response.ok || !data.success) throw new Error(data.message || rideFormText('Vehiculul nu a putut fi salvat.', 'Не удалось сохранить автомобиль.'));
    const select = document.getElementById('vehicle-select');
    select.add(new Option(`${data.vehicle.make} · ${data.vehicle.plateNumber}`, String(data.vehicle.id)));
    select.value = String(data.vehicle.id);
    toggleVehicleForm(false);
    return data.vehicle.id;
}

async function updateRide(rideId) {
    if (isSubmittingRide || !loadedRide) {
        console.log('Form not ready or already submitting');
        return;
    }
    
    document.querySelectorAll('#edit-ride-form > .ride-step').forEach(step => { step.disabled = false; });
    const formData = new FormData(document.getElementById('edit-ride-form'));
    if (!await preparePhoneVisibility(formData)) return;
    
    const isPackageOnly = !editingPassengerRequest && document.getElementById('ride-type-packages-only').checked;
    const availableSeats = isPackageOnly ? 1 : parseInt(formData.get('availableSeats'));
    
    // Verificăm dacă conversiile au fost reușite
    if (isNaN(availableSeats)) {
        showError(rideFormText("Datele introduse pentru locuri disponibile nu sunt valide.", "Введите корректное количество мест."));
        return;
    }
    
    const rideData = {
        fromLocation: formData.get('fromLocation'),
        toLocation: formData.get('toLocation'),
        travelDate: formData.get('travelDate'),
        departureTime: formData.get('flexibleTime') === 'true' ? '00:00' : formData.get('departureTime'),
        flexibleTime: formData.get('flexibleTime') === 'true',
        availableSeats: editingPassengerRequest || isPackageOnly ? 0 : availableSeats,
        description: formData.get('description'),
        showPhoneNumber: formData.get('showPhoneNumber') === 'true',
        contactPhone: formData.get('contactPhone'),
        isPackageOnly,
        transportAndPackages: !editingPassengerRequest && formData.get('rideType') === 'passengers-and-packages',
        vehicleId: !editingPassengerRequest && /^\d+$/.test(formData.get('vehicleId') || '') ? Number(formData.get('vehicleId')) : null,
        announcementType: editingPassengerRequest ? 'PASSENGER_REQUEST' : 'DRIVER_OFFER',
        requestedSeats: editingPassengerRequest ? availableSeats : null
    };
    
    console.log('Updating ride with data:', rideData);
    console.log('Ride ID:', rideId);
    
    // Validare
    if (!validateForm(rideData)) {
        return;
    }

    if (!await confirmPhoneVisibility(rideData.showPhoneNumber)) return;

    let redirecting = false;
    setSubmitState(true);
    if (!editingPassengerRequest && formData.get('vehicleId') === '__new__') {
        try {
            rideData.vehicleId = await createEditVehicle();
        } catch (error) {
            showError(error.message);
            setSubmitState(false);
            return;
        }
    }

    fetch(`/api/rides/${rideId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(rideData)
    })
    .then(response => {
        console.log('Response status:', response.status);
        console.log('Response ok:', response.ok);
        
        if (!response.ok) {
            if (response.status === 401) {
                window.location.href = `/${getCurrentLang()}/login`;
                redirecting = true;
                return null;
            }
            if (response.status === 403) {
                const currentLang = getCurrentLang();
                throw new Error(getEditRideTranslation('permissionError', currentLang));
            }
            return response.json().then(data => {
                console.log('Error response data:', data);
                throw new Error(data.message || 'Eroare la actualizarea cursei');
            });
        }
        return response.json();
    })
    .then(data => {
        if (!data) {
            return;
        }

        console.log('Response data:', data);
        
        if (data.success) {
            const currentLang = getCurrentLang();
            showSuccess(getEditRideTranslation('updateSuccess', currentLang));
            redirecting = true;
            setTimeout(() => {
                window.location.href = data.rideUrl || buildRideUrl(data.ride);
            }, 2000);
        } else {
            const currentLang = getCurrentLang();
            showError(data.message || getEditRideTranslation('updateError', currentLang));
        }
    })
    .catch(error => {
        console.error('Error:', error);
        const currentLang = getCurrentLang();
        showError(error.message || getEditRideTranslation('updateError', currentLang));
    })
    .finally(() => {
        if (!redirecting) {
            setSubmitState(false);
        }
    });
}

function validateForm(data) {
    const currentLang = getCurrentLang();
    
    if (!data.fromLocation || !data.toLocation) {
        showError(getEditRideTranslation('validationErrors.locationsRequired', currentLang));
        return false;
    }
    
    if (!data.travelDate) {
        showError(getEditRideTranslation('validationErrors.dateRequired', currentLang));
        return false;
    }
    
    if (!data.departureTime) {
        showError(getEditRideTranslation('validationErrors.timeRequired', currentLang));
        return false;
    }
    
    const seats = editingPassengerRequest ? data.requestedSeats : data.availableSeats;
    if (!data.isPackageOnly && (!seats || seats < 1 || seats > 100)) {
        showError(getEditRideTranslation('validationErrors.seatsRequired', currentLang));
        return false;
    }

    const selectedVehicle = document.getElementById('vehicle-select').value;
    if (!editingPassengerRequest && !data.vehicleId && selectedVehicle !== '__new__' && selectedVehicle !== '__existing__') {
        showError(currentLang === 'ru' ? 'Выберите автомобиль.' : 'Selectează un vehicul.');
        return false;
    }

    const terms = document.getElementById('terms');
    if (!terms || !terms.checked) {
        showError(currentLang === 'ru'
            ? 'Необходимо принять Условия использования.'
            : 'Trebuie să accepți Termenii și Condițiile.');
        return false;
    }
    
    
    // Pentru editare, nu verificăm dacă data este în trecut
    // Utilizatorul poate edita o cursă care a avut loc deja
    // const selectedDate = new Date(data.travelDate);
    // const today = new Date();
    // today.setHours(0, 0, 0, 0);
    
    // if (selectedDate < today) {
    //     showError(getEditRideTranslation('validationErrors.pastDate', currentLang));
    //     return false;
    // }
    
    return true;
}

function showSuccess(message) {
    // Creăm un element pentru mesajul de succes
    const successDiv = document.createElement('div');
    successDiv.className = 'alert alert-success';
    successDiv.innerHTML = `
        <i class="fas fa-check-circle"></i>
        ${message}
    `;
    
    // Inserăm mesajul înainte de formular
    const form = document.getElementById('edit-ride-form');
    form.parentNode.insertBefore(successDiv, form);
    
    // Eliminăm mesajul după 5 secunde
    setTimeout(() => {
        if (successDiv.parentNode) {
            successDiv.parentNode.removeChild(successDiv);
        }
    }, 5000);
}

function showError(message) {
    // Creăm un element pentru mesajul de eroare
    const errorDiv = document.createElement('div');
    errorDiv.className = 'alert alert-error';
    errorDiv.innerHTML = `
        <i class="fas fa-exclamation-circle"></i>
        ${message}
    `;
    
    // Inserăm mesajul înainte de formular
    const form = document.getElementById('edit-ride-form');
    form.parentNode.insertBefore(errorDiv, form);
    
    // Eliminăm mesajul după 5 secunde
    setTimeout(() => {
        if (errorDiv.parentNode) {
            errorDiv.parentNode.removeChild(errorDiv);
        }
    }, 5000);
}

function updateFlexibleTimeInterface() {
    const flexible = document.getElementById('flexible-time').checked;
    const group = document.getElementById('departure-time-group');
    group.hidden = flexible;
    group.querySelectorAll('input:not([type="hidden"]), button').forEach(control => {
        control.disabled = flexible;
        if (control.tagName === 'INPUT') control.required = !flexible;
    });
}

function updateRideTypeInterface() {
    const packageOnly = !editingPassengerRequest && document.getElementById('ride-type-packages-only').checked;
    document.getElementById('driver-options').hidden = editingPassengerRequest;
    document.getElementById('vehicle-group').hidden = editingPassengerRequest;
    document.getElementById('vehicle-select').required = !editingPassengerRequest;
    document.getElementById('vehicle-select').disabled = editingPassengerRequest;
    toggleVehicleForm(!editingPassengerRequest && document.getElementById('vehicle-select').value === '__new__');
    document.getElementById('seats-group').hidden = packageOnly;
    document.getElementById('available-seats').required = !packageOnly;
    document.getElementById('available-seats').disabled = packageOnly;
    document.querySelector('label[for="available-seats"]').textContent = editingPassengerRequest
        ? rideFormText('Număr de pasageri:', 'Количество пассажиров:')
        : rideFormText('Locuri disponibile:', 'Свободные места:');
    document.querySelector('#edit-ride-form button[type="submit"] span').textContent = editingPassengerRequest
        ? rideFormText('Actualizează cererea', 'Сохранить заявку')
        : rideFormText('Actualizează cursa', 'Сохранить поездку');
}

function initializeMap() {
    const mapElement = document.getElementById('route-map');
    if (!mapElement) {
        console.error('Map element not found');
        return;
    }
    
    try {
        console.log('Initializing map...');
        
        // Inițializăm harta cu centrul pe Moldova
        map = L.map('route-map').setView([47.0105, 28.8638], 8);
        
        // Adăugăm layer-ul OpenStreetMap pentru harta de bază
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);
        
        console.log('Map initialized successfully');
        
        // Inițializăm controalele hărții
        initializeMapControls();
    } catch (error) {
        console.error('Error initializing map:', error);
    }
}

function initializeMapControls() {
    try {
        console.log('Initializing map controls...');
        
        // Handler pentru butonul "Calculează Ruta"
        document.getElementById('calculate-route').addEventListener('click', function() {
            const fromLocation = document.getElementById('from-location').value;
            const toLocation = document.getElementById('to-location').value;
            
            if (fromLocation && toLocation) {
                calculateRoute(fromLocation, toLocation);
            } else {
                showError(rideFormText("Vă rugăm să introduceți locațiile de plecare și destinație", "Введите пункт отправления и пункт назначения"));
            }
        });
        
        // Handler pentru butonul "Șterge Ruta"
        document.getElementById('clear-route').addEventListener('click', function() {
            clearRoute();
        });
        
    } catch (error) {
        console.error('Error initializing map controls:', error);
    }
}

function calculateRoute(fromLocation, toLocation) {
    // Aici poți implementa logica pentru calcularea rutei
    // Pentru moment, doar afișăm un mesaj
    showSuccess(`Ruta de la ${fromLocation} la ${toLocation} a fost calculată!`);
}

function clearRoute() {
    // Aici poți implementa logica pentru ștergerea rutei
    showSuccess('Ruta a fost ștearsă!');
}

// Inițializarea autocomplete pentru localități
function initializeLocationAutocomplete() {
    console.log('Initializing locality autocomplete for edit ride...');
    
    // Verificăm dacă clasa LocalityAutocomplete există
    if (typeof LocalityAutocomplete === 'undefined') {
        console.error('LocalityAutocomplete class not found. Make sure locality-autocomplete.js is loaded.');
        return;
    }
    
    // Inițializăm autocomplete pentru input-ul "from"
    const fromAutocomplete = new LocalityAutocomplete({
        inputSelector: '#from-location',
        resultsContainerSelector: '#from-suggestions',
        language: document.documentElement.lang === 'ru' ? 'ru' : 'ro',
        limit: 10,
        includeDistrict: true
    });
    
    // Inițializăm autocomplete pentru input-ul "to"
    const toAutocomplete = new LocalityAutocomplete({
        inputSelector: '#to-location',
        resultsContainerSelector: '#to-suggestions',
        language: document.documentElement.lang === 'ru' ? 'ru' : 'ro',
        limit: 10,
        includeDistrict: true
    });
    
    // Adăugăm listener pentru evenimentul localitySelected pentru a gestiona markerii pe hartă
    document.addEventListener('localitySelected', function(e) {
        const { locality, input } = e.detail;
        
        if (locality && locality.latitude && locality.longitude) {
            // Determinăm tipul markerului bazat pe ID-ul input-ului
            let markerType;
            if (input.id.includes('from')) {
                markerType = 'from';
            } else if (input.id.includes('to')) {
                markerType = 'to';
            } else {
                markerType = 'unknown';
            }
            
            const localityName = locality.nameRo || locality.nameRu || 'Unknown';
            addMarkerToMap(locality.latitude, locality.longitude, localityName, markerType);
            
            console.log(`Selected locality: ${localityName} (${markerType}) at ${locality.latitude}, ${locality.longitude}`);
        }
    });
    
    console.log('Locality autocomplete initialized successfully for edit ride');
}

// Adăugarea unui marker pe hartă
function addMarkerToMap(lat, lon, name, type) {
    // Verificăm dacă harta și Leaflet sunt disponibile
    if (!map || typeof L === 'undefined') {
        console.error('Map or Leaflet not available');
        return;
    }
    
    console.log(`Adding marker: ${name} at ${lat}, ${lon} (${type})`);
    
    const marker = L.marker([lat, lon]).addTo(map);
    
    if (type === 'from') {
        if (window.fromMarker) map.removeLayer(window.fromMarker);
        window.fromMarker = marker;
        marker.setIcon(L.divIcon({
            className: 'custom-marker from-marker',
            html: '<i class="fas fa-map-marker-alt" style="color: #3b82f6;"></i>',
            iconSize: [30, 30]
        }));
    } else {
        if (window.toMarker) map.removeLayer(window.toMarker);
        window.toMarker = marker;
        marker.setIcon(L.divIcon({
            className: 'custom-marker to-marker',
            html: '<i class="fas fa-map-marker-alt" style="color: #ef4444;"></i>',
            iconSize: [30, 30]
        }));
    }
    
    marker.bindPopup(`<b>${name}</b>`);
    
    // Centrăm harta pe ambele markeri dacă există
    if (window.fromMarker && window.toMarker) {
        const group = L.featureGroup([window.fromMarker, window.toMarker]);
        map.fitBounds(group.getBounds().pad(0.1));
    }
}

// Afișarea previzualizării
function showPreview() {
    console.log('Showing preview for edit ride...');
    
    if (!validateFormForPreview()) {
        console.log('Form validation failed for preview');
        return;
    }
    
    const form = document.getElementById('edit-ride-form');
    if (!form) {
        console.error('Edit ride form not found');
        return;
    }
    
    const formData = new FormData(form);
    const currentFormData = Object.fromEntries(formData);
    
    console.log('Form data collected for preview:', currentFormData);
    
    const previewContent = document.getElementById('preview-content');
    const modal = document.getElementById('preview-modal');
    
    if (previewContent) {
        previewContent.innerHTML = generatePreviewHTML(currentFormData);
        console.log('Preview content generated');
    } else {
        console.error('Preview content element not found');
        return;
    }
    
    if (modal) {
        modal.style.display = 'block';
        console.log('Modal displayed');
    } else {
        console.error('Preview modal not found');
    }
}

// Validarea formularului pentru previzualizare
function validateFormForPreview() {
    console.log('Validating form for preview...');
    
    // Verificăm tipul de transport selectat
    const isPackageOnly = !editingPassengerRequest && document.getElementById('ride-type-packages-only').checked;

    const terms = document.getElementById('terms');
    if (!terms || !terms.checked) {
        showError(getCurrentLang() === 'ru'
            ? 'Необходимо принять Условия использования.'
            : 'Trebuie să accepți Termenii și Condițiile.');
        return false;
    }
    
    // Câmpurile obligatorii diferă în funcție de tipul de transport
    const requiredFields = ['fromLocation', 'toLocation', 'travelDate'];
    if (!document.getElementById('flexible-time').checked) requiredFields.push('departureTime');
    if (!editingPassengerRequest) {
        requiredFields.push('vehicleId');
    }
    
    // Adăugăm availableSeats doar pentru transport pasageri
    if (!isPackageOnly) {
        requiredFields.push('availableSeats');
    }
    
    for (const field of requiredFields) {
        const element = document.querySelector(`[name="${field}"]`);
        if (!element) {
            console.error(`Required field element not found: ${field}`);
            showError(rideFormText(`Câmpul "${field}" nu a fost găsit.`, `Поле «${field}» не найдено.`));
            return false;
        }
        
        if (!element.value.trim()) {
            console.log(`Field ${field} is empty`);
            showError(rideFormText(`Câmpul "${element.placeholder || field}" este obligatoriu.`, `Заполните поле «${element.placeholder || field}».`));
            return false;
        }
    }
    
    
    // Validare locuri disponibile (doar pentru transport pasageri)
    const seatsElement = document.getElementById('available-seats');
    if (seatsElement && !isPackageOnly) {
        const seats = parseInt(seatsElement.value);
        if (seats < 1 || seats > 100) {
            console.log('Seats validation failed:', seats);
            showError(rideFormText("Numărul de locuri disponibile trebuie să fie între 1 și 100.", "Количество мест должно быть от 1 до 100."));
            return false;
        }
    }
    
    return true;
}

// Generarea HTML-ului pentru previzualizare
function generatePreviewHTML(data) {
    console.log('Generating preview HTML for data:', data);
    
    const isPackageOnly = !editingPassengerRequest && document.getElementById('ride-type-packages-only').checked;
    
    const transportAndPackages = !editingPassengerRequest && document.getElementById('ride-type-passengers-and-packages').checked;
    
    return `
        <div class="preview-ride">
            <div class="preview-section">
                <h4><i class="fas fa-route"></i> ${rideFormText("Ruta", "Маршрут")}</h4>
                <p><strong>${rideFormText("De la:", "Откуда:")}</strong> ${data.fromLocation || 'N/A'}</p>
                <p><strong>${rideFormText("Până la:", "Куда:")}</strong> ${data.toLocation || 'N/A'}</p>
            </div>
            
            <div class="preview-section">
                <h4><i class="fas fa-calendar"></i> ${rideFormText("Detalii Călătorie", "Детали поездки")}</h4>
                <p><strong>${rideFormText("Data:", "Дата:")}</strong> ${data.travelDate || 'N/A'}</p>
                <p><strong>${rideFormText("Ora plecării:", "Время отправления:")}</strong> ${data.flexibleTime === 'true' ? rideFormText('Oră flexibilă', 'Гибкое время') : (data.departureTime || 'N/A')}</p>
                ${isPackageOnly ? 
                    `<p><strong>${rideFormText("Tip transport:", "Тип перевозки:")}</strong> <i class="fas fa-box"></i> ${rideFormText("Transport doar colete", "Только посылки")}</p>` :
                    `<p><strong>${rideFormText("Locuri disponibile:", "Свободные места:")}</strong> ${data.availableSeats || 'N/A'}</p>`
                }
                ${!isPackageOnly && transportAndPackages ? 
                    `<p><strong>${rideFormText("Servicii:", "Услуги:")}</strong> <i class="fas fa-box" style="color: #3b82f6;"></i> ${rideFormText("Transport și colete", "Также перевожу посылки")}</p>` : ''
                }
            </div>
            
            ${data.description ? `
                <div class="preview-section">
                    <h4><i class="fas fa-info-circle"></i> ${rideFormText("Descriere", "Описание")}</h4>
                    <p>${data.description}</p>
                </div>
            ` : ''}
        </div>
    `;
}

// Închiderea modalului
function closeModal() {
    const modal = document.getElementById('preview-modal');
    if (modal) {
        modal.style.display = 'none';
        console.log('Modal closed');
    } else {
        console.warn('Preview modal not found');
    }
}

// Submit-ul din modal pentru editare
function submitRide() {
    console.log('Submitting ride update from modal...');

    if (isSubmittingRide) {
        console.log('Submission already in progress, ignoring.');
        return;
    }
    
    const rideId = getRideIdFromUrl();
    if (!rideId) {
        showError(rideFormText("ID-ul cursei nu a fost găsit.", "Не найден идентификатор объявления."));
        return;
    }
    
    // Închidem modalul
    closeModal();
    
    // Actualizăm cursa
    updateRide(rideId);
}
let editingPassengerRequest = false;
