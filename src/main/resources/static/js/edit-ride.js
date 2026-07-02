let map;
let isSubmittingRide = false;

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
                ? '<i class="fas fa-spinner fa-spin"></i> Se actualizează...'
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
    
    // Initialize translations
    const currentLang = getCurrentLang();
    updateEditRideTranslations(currentLang);
    
    // Initialize map
    try {
        initializeMap();
        console.log('Map initialized');
    } catch (error) {
        console.error('Error initializing map:', error);
    }
    
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
    
    // Handler pentru radio buttons pentru tipul de transport
    document.getElementById('ride-type-passengers').addEventListener('change', function() {
        if (this.checked) {
            updateRideTypeInterface();
        }
    });
    
    document.getElementById('ride-type-packages').addEventListener('change', function() {
        if (this.checked) {
            updateRideTypeInterface();
        }
    });
    
    // Handler pentru checkbox transport și colete
    document.getElementById('transport-and-packages').addEventListener('change', function() {
        // Logica pentru checkbox-ul de transport și colete
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
                window.location.href = '/ro/login';
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
        
        if (data.success) {
            console.log('Ride data to populate:', data.ride);
            populateForm(data.ride);
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
        
        // Formatăm data pentru input type="date"
        if (ride.travelDate && travelDateElement) {
            console.log('Original travel date:', ride.travelDate);
            const date = new Date(ride.travelDate);
            const formattedDate = date.toISOString().split('T')[0];
            console.log('Formatted date:', formattedDate);
            travelDateElement.value = formattedDate;
        }
        
        // Formatăm timpul pentru input type="time"
        if (ride.departureTime && departureTimeElement) {
            console.log('Original departure time:', ride.departureTime);
            const time = new Date(ride.departureTime);
            const formattedTime = time.toTimeString().slice(0, 5);
            console.log('Formatted time:', formattedTime);
            departureTimeElement.value = formattedTime;
        }
        
        if (availableSeatsElement) availableSeatsElement.value = ride.availableSeats || '';
        if (descriptionElement) descriptionElement.value = ride.description || '';
        
        // Setăm checkbox-urile pentru tipul de transport
        const rideTypePackages = document.getElementById('ride-type-packages');
        const rideTypePassengers = document.getElementById('ride-type-passengers');
        
        if (rideTypePackages && rideTypePassengers) {
            if (ride.isPackageOnly) {
                rideTypePackages.checked = true;
                rideTypePassengers.checked = false;
            } else {
                rideTypePassengers.checked = true;
                rideTypePackages.checked = false;
            }
        }
        
        const transportAndPackages = document.getElementById('transport-and-packages');
        if (transportAndPackages) {
            transportAndPackages.checked = ride.transportAndPackages || false;
        }
        
        // Actualizăm interfața în funcție de tipul de transport selectat
        updateRideTypeInterface();
        
        console.log('Form populated successfully');
    } catch (error) {
        console.error('Error populating form:', error);
    }
}

function updateRide(rideId) {
    if (isSubmittingRide) {
        console.log('Form already submitting, ignoring...');
        return;
    }
    
    const formData = new FormData(document.getElementById('edit-ride-form'));
    
    const availableSeats = parseInt(formData.get('availableSeats'));
    
    // Verificăm dacă conversiile au fost reușite
    if (isNaN(availableSeats)) {
        showError('Datele introduse pentru locuri disponibile nu sunt valide.');
        return;
    }
    
    const rideData = {
        fromLocation: formData.get('fromLocation'),
        toLocation: formData.get('toLocation'),
        travelDate: formData.get('travelDate'),
        departureTime: formData.get('departureTime'),
        availableSeats: availableSeats,
        description: formData.get('description'),
        isPackageOnly: document.getElementById('ride-type-packages').checked,
        transportAndPackages: formData.get('transportAndPackages') === 'on'
    };
    
    console.log('Updating ride with data:', rideData);
    console.log('Ride ID:', rideId);
    
    // Validare
    if (!validateForm(rideData)) {
        return;
    }

    let redirecting = false;
    setSubmitState(true);
    
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
    
    if (!data.availableSeats || data.availableSeats < 1 || data.availableSeats > 100) {
        showError(getEditRideTranslation('validationErrors.seatsRequired', currentLang));
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

function updateRideTypeInterface() {
    const isPackageOnly = document.getElementById('ride-type-packages').checked;
    const seatsGroup = document.getElementById('seats-group');
    const transportPackagesGroup = document.getElementById('transport-packages-group');
    
    if (isPackageOnly) {
        // Pentru transport doar colete
        seatsGroup.style.display = 'none';
        transportPackagesGroup.style.display = 'none';
    } else {
        // Pentru transport pasageri
        seatsGroup.style.display = 'block';
        transportPackagesGroup.style.display = 'block';
    }
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
                showError('Vă rugăm să introduceți locațiile de plecare și destinație');
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
        language: 'ro',
        limit: 10,
        includeDistrict: true
    });
    
    // Inițializăm autocomplete pentru input-ul "to"
    const toAutocomplete = new LocalityAutocomplete({
        inputSelector: '#to-location',
        resultsContainerSelector: '#to-suggestions',
        language: 'ro',
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
    const passengerRadio = document.getElementById('ride-type-passengers');
    const packageRadio = document.getElementById('ride-type-packages');
    
    if (!passengerRadio.checked && !packageRadio.checked) {
        showError('Vă rugăm să selectați tipul de transport.');
        return false;
    }
    
    const isPackageOnly = packageRadio.checked;
    
    // Câmpurile obligatorii diferă în funcție de tipul de transport
    const requiredFields = ['fromLocation', 'toLocation', 'travelDate', 'departureTime'];
    
    // Adăugăm availableSeats doar pentru transport pasageri
    if (!isPackageOnly) {
        requiredFields.push('availableSeats');
    }
    
    for (const field of requiredFields) {
        const element = document.querySelector(`[name="${field}"]`);
        if (!element) {
            console.error(`Required field element not found: ${field}`);
            showError(`Câmpul "${field}" nu a fost găsit.`);
            return false;
        }
        
        if (!element.value.trim()) {
            console.log(`Field ${field} is empty`);
            showError(`Câmpul "${element.placeholder || field}" este obligatoriu.`);
            return false;
        }
    }
    
    
    // Validare locuri disponibile (doar pentru transport pasageri)
    const seatsElement = document.getElementById('available-seats');
    if (seatsElement && !isPackageOnly) {
        const seats = parseInt(seatsElement.value);
        if (seats < 1 || seats > 100) {
            console.log('Seats validation failed:', seats);
            showError('Numărul de locuri disponibile trebuie să fie între 1 și 100.');
            return false;
        }
    }
    
    return true;
}

// Generarea HTML-ului pentru previzualizare
function generatePreviewHTML(data) {
    console.log('Generating preview HTML for data:', data);
    
    const packageRadio = document.getElementById('ride-type-packages');
    const isPackageOnly = packageRadio ? packageRadio.checked : false;
    
    const transportAndPackagesCheckbox = document.getElementById('transport-and-packages');
    const transportAndPackages = transportAndPackagesCheckbox ? transportAndPackagesCheckbox.checked : false;
    
    return `
        <div class="preview-ride">
            <div class="preview-section">
                <h4><i class="fas fa-route"></i> Ruta</h4>
                <p><strong>De la:</strong> ${data.fromLocation || 'N/A'}</p>
                <p><strong>Până la:</strong> ${data.toLocation || 'N/A'}</p>
            </div>
            
            <div class="preview-section">
                <h4><i class="fas fa-calendar"></i> Detalii Călătorie</h4>
                <p><strong>Data:</strong> ${data.travelDate || 'N/A'}</p>
                <p><strong>Ora plecării:</strong> ${data.departureTime || 'N/A'}</p>
                ${isPackageOnly ? 
                    '<p><strong>Tip transport:</strong> <i class="fas fa-box"></i> Transport doar colete</p>' :
                    `<p><strong>Locuri disponibile:</strong> ${data.availableSeats || 'N/A'}</p>`
                }
                ${!isPackageOnly && transportAndPackages ? 
                    '<p><strong>Servicii:</strong> <i class="fas fa-box" style="color: #3b82f6;"></i> Transport și colete</p>' : ''
                }
            </div>
            
            ${data.description ? `
                <div class="preview-section">
                    <h4><i class="fas fa-info-circle"></i> Descriere</h4>
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
        showError('ID-ul cursei nu a fost găsit.');
        return;
    }
    
    // Închidem modalul
    closeModal();
    
    // Actualizăm cursa
    updateRide(rideId);
}
