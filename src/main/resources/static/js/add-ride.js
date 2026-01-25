// Variabile globale pentru harta și autocomplete
let map;
let fromMarker, toMarker;
let routeLayer;
let currentFormData = {};
let selectedLocalities = { from: null, to: null }; // Pentru a ține evidența localităților selectate

function translateText(key, defaultText) {
    const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
    if (currentLang === 'ro') {
        return defaultText;
    }

    const translations = {
        'add_ride.location_warning': 'Рекомендуем выбирать населенный пункт из подсказок для лучшего опыта.',
        'add_ride.location_warning_missing': 'Пожалуйста, выберите пункт отправления и пункт назначения.',
        'add_ride.location_warning_continue': 'Вы можете продолжить, но рекомендуем выбирать населенные пункты из подсказок.'
    };

    return translations[key] || defaultText;
}
let isSubmittingRide = false;
let vehiclesCache = [];

function setSubmitState(isSubmitting) {
    isSubmittingRide = isSubmitting;
    const submitBtn = document.querySelector('.add-ride-form .btn.btn-primary[type="submit"]');
    const previewBtn = document.getElementById('preview-ride');
    const modalSubmitBtn = document.querySelector('#preview-modal .modal-footer .btn.btn-primary');

    [submitBtn, previewBtn, modalSubmitBtn].forEach(btn => {
        if (!btn) {
            return;
        }
        btn.disabled = isSubmitting;
        btn.classList.toggle('is-submitting', isSubmitting);
        btn.setAttribute('aria-busy', isSubmitting ? 'true' : 'false');
    });
}

// Inițializare când se încarcă pagina
document.addEventListener('DOMContentLoaded', function() {
    // Add-ride page loaded, initializing...
    
    // Verificăm dacă utilizatorul este autentificat
    checkAuthentication();
    
    try {
        initializeMap();
        // Map initialized
    } catch (error) {
        console.error('Error initializing map:', error);
    }
    
    try {
        initializeNewLocationAutocomplete();
        // New autocomplete initialized
    } catch (error) {
        console.error('Error initializing new autocomplete:', error);
    }
    
    try {
        initializeFormHandlers();
        // Form handlers initialized
    } catch (error) {
        console.error('Error initializing form handlers:', error);
    }
    
    try {
        setDefaultDate();
        // Default date set
    } catch (error) {
        console.error('Error setting default date:', error);
    }
    
    try {
        initializeModernCalendar();
        // Modern calendar initialized
    } catch (error) {
        console.error('Error initializing modern calendar:', error);
    }
    
    try {
        initializeRideTypeHandlers();
        // Ride type handlers initialized
    } catch (error) {
        console.error('Error initializing ride type handlers:', error);
    }

    try {
        initializeVehicleHandlers();
    } catch (error) {
        console.error('Error initializing vehicle handlers:', error);
    }

    try {
        initializeSeatsInput();
        // Seats input initialized
    } catch (error) {
        console.error('Error initializing seats input:', error);
    }
});

// Funcție pentru verificarea autentificării
function checkAuthentication() {
    // Pentru testare, să nu redirecționăm automat
            // Checking authentication...
    
    fetch('/api/auth/user')
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                return null;
            }
        })
        .then(user => {
            if (!user) {
                // User not authenticated, but continuing for testing
                // Pentru testare, nu redirecționăm automat
                // sessionStorage.setItem('redirectAfterLogin', '/add-ride');
                // window.location.href = '/login';
            } else {
                // User authenticated
            }
        })
        .catch(error => {
            console.error('Error checking auth status:', error);
            // Pentru testare, nu redirecționăm automat în caz de eroare
            // sessionStorage.setItem('redirectAfterLogin', '/add-ride');
            // window.location.href = '/login';
        });
}

// Inițializarea hărții
function initializeMap() {
    const mapElement = document.getElementById('route-map');
    if (!mapElement) {
        console.error('Map element not found');
        return;
    }
    
    // Verificăm dacă Leaflet este disponibil
    if (typeof L === 'undefined') {
        console.error('Leaflet not loaded');
        return;
    }
    
    // Initializing map...
    
    try {
        // Centrul hărții pe Moldova
        map = L.map('route-map').setView([47.0105, 28.8638], 8);
        
        // Adăugăm layer-ul OpenStreetMap pentru harta de bază
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);
        
        // Map initialized successfully
        
        // Inițializăm controalele hărții
        initializeMapControls();
    } catch (error) {
        console.error('Error initializing map:', error);
    }
}

// Inițializarea controalelor hărții
function initializeMapControls() {
    const calculateRouteBtn = document.getElementById('calculate-route');
    const clearRouteBtn = document.getElementById('clear-route');
    
    // Initializing map controls...
    
    if (calculateRouteBtn) {
        calculateRouteBtn.addEventListener('click', calculateRoute);
        // Calculate route button handler added
    } else {
        console.warn('Calculate route button not found');
    }
    
    if (clearRouteBtn) {
        clearRouteBtn.addEventListener('click', clearRoute);
        // Clear route button handler added
    } else {
        console.warn('Clear route button not found');
    }
}

// Inițializarea noului autocomplete pentru localități
function initializeNewLocationAutocomplete() {
    // Initializing new locality autocomplete...
    
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
                selectedLocalities.from = locality; // Salvăm localitatea selectată
                clearLocationWarning('from'); // Ștergem avertismentul
                // Localitate selectată pentru "de la"
            } else if (input.id.includes('to')) {
                markerType = 'to';
                selectedLocalities.to = locality; // Salvăm localitatea selectată
                clearLocationWarning('to'); // Ștergem avertismentul
                // Localitate selectată pentru "până la"
            } else {
                markerType = 'unknown';
            }
            
            const localityName = locality.nameRo || locality.nameRu || 'Unknown';
            addMarkerToMap(locality.latitude, locality.longitude, localityName, markerType);
            
            // Selected locality coordinates
        }
    });
    
    // Adăugăm listener pentru input events pentru a detecta când utilizatorul scrie manual
    const fromInput = document.getElementById('from-location');
    const toInput = document.getElementById('to-location');
    
    if (fromInput) {
        fromInput.addEventListener('input', function() {
            // Dacă utilizatorul scrie manual și nu conține virgulă (indicând o localitate selectată din sugestii)
            if (!this.value.includes(',')) {
                selectedLocalities.from = null;
            }
            // Validăm în timp real
            validateLocationInRealTime('from');
        });
        
        // Adăugăm listener pentru focus pentru a detecta când utilizatorul începe să scrie
        fromInput.addEventListener('focus', function() {
            // Dacă input-ul nu conține virgulă, înseamnă că nu a fost selectată din sugestii
            if (this.value.trim() && !this.value.includes(',')) {
                selectedLocalities.from = null;
            }
        });
        
        // Adăugăm listener pentru blur pentru a valida când utilizatorul termină de scris
        fromInput.addEventListener('blur', function() {
            validateLocationInRealTime('from');
        });
    }
    
    if (toInput) {
        toInput.addEventListener('input', function() {
            // Dacă utilizatorul scrie manual și nu conține virgulă (indicând o localitate selectată din sugestii)
            if (!this.value.includes(',')) {
                selectedLocalities.to = null;
            }
            // Validăm în timp real
            validateLocationInRealTime('to');
        });
        
        // Adăugăm listener pentru focus pentru a detecta când utilizatorul începe să scrie
        toInput.addEventListener('focus', function() {
            // Dacă input-ul nu conține virgulă, înseamnă că nu a fost selectată din sugestii
            if (this.value.trim() && !this.value.includes(',')) {
                selectedLocalities.to = null;
            }
        });
        
        // Adăugăm listener pentru blur pentru a valida când utilizatorul termină de scris
        toInput.addEventListener('blur', function() {
            validateLocationInRealTime('to');
        });
    }
    
    console.log('New locality autocomplete initialized successfully');
}

// Funcțiile de autocomplete au fost mutate în locality-autocomplete.js (Google Places API)

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
        if (fromMarker) map.removeLayer(fromMarker);
        fromMarker = marker;
        marker.setIcon(L.divIcon({
            className: 'custom-marker from-marker',
            html: '<i class="fas fa-map-marker-alt" style="color: #3b82f6;"></i>',
            iconSize: [30, 30]
        }));
    } else {
        if (toMarker) map.removeLayer(toMarker);
        toMarker = marker;
        marker.setIcon(L.divIcon({
            className: 'custom-marker to-marker',
            html: '<i class="fas fa-map-marker-alt" style="color: #ef4444;"></i>',
            iconSize: [30, 30]
        }));
    }
    
    marker.bindPopup(`<b>${name}</b>`);
    
    // Centrăm harta pe ambele markeri dacă există
    if (fromMarker && toMarker) {
        const group = L.featureGroup([fromMarker, toMarker]);
        map.fitBounds(group.getBounds().pad(0.1));
    }
}

// Ascunderea sugestiilor
function hideSuggestions(container) {
    if (container) {
        container.style.display = 'none';
    }
}

// Funcția pentru afișarea notificărilor
function showNotification(message, type = 'info') {
    // Ștergem notificările existente
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(notification => notification.remove());
    
    const notification = document.createElement('div');
    notification.className = 'notification';
    
    const iconMap = {
        success: 'fas fa-check-circle',
        error: 'fas fa-exclamation-circle',
        warning: 'fas fa-exclamation-triangle',
        info: 'fas fa-info-circle'
    };
    
    notification.innerHTML = `
        <div class="notification-content ${type}">
            <i class="${iconMap[type] || iconMap.info}"></i>
            <span>${message}</span>
        </div>
    `;
    
    document.body.appendChild(notification);
    
    // Ștergem notificarea după 5 secunde
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 5000);
}

// Funcție pentru afișarea avertismentului pentru localități
function showLocationWarning(field, message) {
    const input = document.getElementById(`${field}-location`);
    if (!input) {
        console.error(`Input not found for showLocationWarning: ${field}`);
        return;
    }
    
    const container = input.parentElement;
    if (!container) {
        console.error(`Container not found for input: ${field}`);
        return;
    }
    
    console.log(`Showing location warning for ${field}: ${message}`);
    
    // Ștergem avertismentul anterior dacă există
    clearLocationWarning(field);
    
    // Adăugăm clasa de avertisment la input
    input.classList.add('location-warning');
    console.log(`Added location-warning class to ${field} input`);
    
    // Creăm elementul de avertisment
    const warningElement = document.createElement('div');
    warningElement.className = 'location-warning-message';
    warningElement.innerHTML = `
        <i class="fas fa-exclamation-triangle"></i>
        <span>${message}</span>
    `;
    
    // Adăugăm avertismentul după input
    container.appendChild(warningElement);
    console.log(`Added warning element to ${field} container`);
}

// Funcție pentru ștergerea avertismentului pentru localități
function clearLocationWarning(field) {
    const input = document.getElementById(`${field}-location`);
    if (!input) {
        console.error(`Input not found for clearLocationWarning: ${field}`);
        return;
    }
    
    const container = input.parentElement;
    if (!container) {
        console.error(`Container not found for input: ${field}`);
        return;
    }
    
    console.log(`Clearing location warning for ${field}`);
    
    // Ștergem clasa de avertisment
    input.classList.remove('location-warning');
    console.log(`Removed location-warning class from ${field} input`);
    
    // Ștergem elementul de avertisment dacă există
    const warningElement = container.querySelector('.location-warning-message');
    if (warningElement) {
        warningElement.remove();
        console.log(`Removed warning element from ${field} container`);
    } else {
        console.log(`No warning element found for ${field}`);
    }
}

// Funcție pentru validarea localităților în timp real
function validateLocationInRealTime(field) {
    const input = document.getElementById(`${field}-location`);
    if (!input) {
        console.log(`Input not found for field: ${field}`);
        return;
    }
    
    const value = input.value.trim();
    console.log(`Validating ${field} with value: "${value}"`);
    
    // Dacă input-ul este gol, ștergem avertismentul
    if (!value) {
        clearLocationWarning(field);
        console.log(`Empty value for ${field}, clearing warning`);
        return;
    }
    
    // Verificăm dacă conține virgulă (indicând o localitate selectată din sugestii) sau dacă avem obiectul locality salvat
    const hasComma = value.includes(',');
    const hasSelectedLocality = field === 'from' ? selectedLocalities.from : selectedLocalities.to;
    
    console.log(`${field} - hasComma: ${hasComma}, hasSelectedLocality: ${!!hasSelectedLocality}`);
    
    if (!hasComma && !hasSelectedLocality) {
        showLocationWarning(field, translateText('add_ride.location_warning', 'Vă recomandăm să selectați o localitate din sugestiile afișate pentru o experiență mai bună.'));
        console.log(`Showing warning for ${field}`);
    } else {
        clearLocationWarning(field);
        console.log(`Clearing warning for ${field}`);
    }
}

// Funcție pentru validarea localităților (pentru submit)
function validateLocations() {
    const fromInput = document.getElementById('from-location');
    const toInput = document.getElementById('to-location');
    let hasWarnings = false;
    
    // Verificăm dacă localitatea "de la" a fost selectată din sugestii
    if (fromInput.value.trim()) {
        // Verificăm dacă conține virgulă (indicând o localitate selectată din sugestii) sau dacă avem obiectul locality salvat
        const hasComma = fromInput.value.includes(',');
        if (!hasComma && !selectedLocalities.from) {
            showLocationWarning('from', translateText('add_ride.location_warning', 'Vă recomandăm să selectați o localitate din sugestiile afișate pentru o experiență mai bună.'));
            hasWarnings = true;
        } else {
            clearLocationWarning('from');
        }
    } else {
        clearLocationWarning('from');
    }
    
    // Verificăm dacă localitatea "până la" a fost selectată din sugestii
    if (toInput.value.trim()) {
        // Verificăm dacă conține virgulă (indicând o localitate selectată din sugestii) sau dacă avem obiectul locality salvat
        const hasComma = toInput.value.includes(',');
        if (!hasComma && !selectedLocalities.to) {
            showLocationWarning('to', translateText('add_ride.location_warning', 'Vă recomandăm să selectați o localitate din sugestiile afișate pentru o experiență mai bună.'));
            hasWarnings = true;
        } else {
            clearLocationWarning('to');
        }
    } else {
        clearLocationWarning('to');
    }
    
    return hasWarnings;
}

// Calcularea rutei
async function calculateRoute() {
    if (!fromMarker || !toMarker) {
        showNotification(translateText('add_ride.location_warning_missing', 'Vă rugăm să selectați atât punctul de plecare cât și cel de destinație.'), 'warning');
        return;
    }
    
    const fromLat = fromMarker.getLatLng().lat;
    const fromLon = fromMarker.getLatLng().lng;
    const toLat = toMarker.getLatLng().lat;
    const toLon = toMarker.getLatLng().lng;
    
    console.log(`Calculating route from ${fromLat}, ${fromLon} to ${toLat}, ${toLon}`);
    
    try {
        // Folosim OSRM pentru calculul rutei
        const url = `https://router.project-osrm.org/route/v1/driving/${fromLon},${fromLat};${toLon},${toLat}?overview=full&geometries=geojson`;
        
        console.log('Fetching route from:', url);
        
        const response = await fetch(url);
        const data = await response.json();
        
        console.log('Route response:', data);
        
        if (data.routes && data.routes.length > 0) {
            displayRoute(data.routes[0]);
            showNotification('Ruta a fost calculată cu succes!', 'success');
        } else {
            showNotification('Nu s-a putut calcula ruta. Vă rugăm să încercați din nou.', 'error');
        }
    } catch (error) {
        console.error('Eroare la calcularea rutei:', error);
        showNotification('Eroare la calcularea rutei. Vă rugăm să încercați din nou.', 'error');
    }
}

// Afișarea rutei pe hartă
function displayRoute(route) {
    // Verificăm dacă harta și Leaflet sunt disponibile
    if (!map || typeof L === 'undefined') {
        console.error('Map or Leaflet not available for displaying route');
        return;
    }
    
    console.log('Displaying route:', route);
    
    // Ștergem ruta anterioară
    if (routeLayer) {
        map.removeLayer(routeLayer);
    }
    
    // Adăugăm noua rută
    routeLayer = L.geoJSON(route.geometry, {
        style: {
            color: '#10b981',
            weight: 4,
            opacity: 0.8
        }
    }).addTo(map);
    
    // Centrăm harta pe rută
    map.fitBounds(routeLayer.getBounds().pad(0.1));
}

// Ștergerea rutei
function clearRoute() {
    console.log('Clearing route...');
    
    if (routeLayer) {
        map.removeLayer(routeLayer);
        routeLayer = null;
        console.log('Route layer cleared');
    }
    
    if (fromMarker) {
        map.removeLayer(fromMarker);
        fromMarker = null;
        console.log('From marker cleared');
    }
    
    if (toMarker) {
        map.removeLayer(toMarker);
        toMarker = null;
        console.log('To marker cleared');
    }
    
    // Resetăm input-urile
    const fromInput = document.getElementById('from-location');
    const toInput = document.getElementById('to-location');
    
    if (fromInput) {
        fromInput.value = '';
        selectedLocalities.from = null;
        clearLocationWarning('from');
    }
    if (toInput) {
        toInput.value = '';
        selectedLocalities.to = null;
        clearLocationWarning('to');
    }
    
    showNotification('Ruta a fost ștearsă.', 'info');
}

// Inițializarea handler-elor pentru formular
function initializeFormHandlers() {
    console.log('Initializing form handlers...');
    
    const form = document.getElementById('add-ride-form');
    const previewBtn = document.getElementById('preview-ride');
    
    console.log('Form found:', !!form);
    console.log('Preview button found:', !!previewBtn);
    
    if (form) {
        form.addEventListener('submit', handleFormSubmit);
        console.log('Form submit handler added');
    } else {
        console.warn('Add ride form not found');
    }
    
    if (previewBtn) {
        previewBtn.addEventListener('click', showPreview);
        console.log('Preview button handler added');
    } else {
        console.warn('Preview button not found');
    }
}

// Handler pentru submit-ul formularului
function handleFormSubmit(e) {
    console.log('Form submit handler called');
    e.preventDefault();

    if (isSubmittingRide) {
        console.log('Submission already in progress, ignoring.');
        return;
    }
    
    if (!validateForm()) {
        console.log('Form validation failed, stopping submission');
        return;
    }
    
    const formData = new FormData(e.target);
    console.log('FormData created from form');
    submitRideData(formData);
}

// Validarea formularului
function validateForm() {
    console.log('Validating form...');
    
    // Verificăm tipul de transport selectat
    const passengersOnlyRadio = document.getElementById('ride-type-passengers-only');
    const packagesOnlyRadio = document.getElementById('ride-type-packages-only');
    const passengersAndPackagesRadio = document.getElementById('ride-type-passengers-and-packages');
    
    if (!passengersOnlyRadio.checked && !packagesOnlyRadio.checked && !passengersAndPackagesRadio.checked) {
        showNotification('Vă rugăm să selectați tipul de transport.', 'error');
        return false;
    }
    
    const isPackageOnly = packagesOnlyRadio.checked;
    
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
            showNotification(`Câmpul "${field}" nu a fost găsit.`, 'error');
            return false;
        }
        
        if (!element.value.trim()) {
            console.log(`Field ${field} is empty`);
            showNotification(`Câmpul "${element.placeholder || field}" este obligatoriu.`, 'error');
            return false;
        }
    }

    const vehicleSelect = document.getElementById('vehicle-select');
    if (vehicleSelect) {
        const selectedValue = vehicleSelect.value;
        if (!selectedValue) {
            showNotification(getVehicleText('selectError', 'Selectați un vehicul pentru această cursă.'), 'error');
            return false;
        }
        if (selectedValue === '__new__') {
            const makeInput = document.getElementById('vehicle-make');
            const colorInput = document.getElementById('vehicle-color');
            const plateInput = document.getElementById('vehicle-plate');
            const make = makeInput ? makeInput.value.trim() : '';
            const color = colorInput ? colorInput.value.trim() : '';
            const plateNumber = plateInput ? plateInput.value.trim() : '';
            if (!make || !color || !plateNumber) {
                showNotification(getVehicleText('fillError', 'Completați marca, culoarea și numărul mașinii.'), 'error');
                return false;
            }
        }
    }
    
    // Validare preț
    
    // Validare locuri disponibile (doar pentru transport pasageri)
    const seatsElement = document.getElementById('available-seats');
    if (seatsElement && !isPackageOnly) {
        const seats = parseInt(seatsElement.value);
        if (seats < 1 || seats > 100) {
            console.log('Seats validation failed:', seats);
            showNotification('Numărul de locuri disponibile trebuie să fie între 1 și 100.', 'error');
            return false;
        }
    }
    
    // Validare localități (afișează avertismente dar permite continuarea)
    const hasLocationWarnings = validateLocations();
    
    if (hasLocationWarnings) {
        // Afișăm un mesaj de informare că poate continua
        showNotification(translateText('add_ride.location_warning_continue', 'Puteți continua cu postarea, dar vă recomandăm să selectați localitățile din sugestii pentru o experiență mai bună.'), 'warning');
    }
    
    console.log('Form validation passed');
    return true;
}

// Trimiterea datelor cursei
async function submitRideData(formData) {
    console.log('Submitting ride data...');

    if (isSubmittingRide) {
        console.log('Submission already in progress, ignoring.');
        return;
    }
    setSubmitState(true);
    
    // Adăugăm câmpul isPackageOnly
    const packagesOnlyRadio = document.getElementById('ride-type-packages-only');
    const isPackageOnly = packagesOnlyRadio ? packagesOnlyRadio.checked : false;
    
    const vehicleSelect = document.getElementById('vehicle-select');
    let resolvedVehicleId = vehicleSelect ? vehicleSelect.value : '';

    if (vehicleSelect && vehicleSelect.value === '__new__') {
        try {
            const createdVehicle = await createVehicleFromForm();
            resolvedVehicleId = createdVehicle.id;
        } catch (error) {
            setSubmitState(false);
            return;
        }
    }

    // Convert FormData to URLSearchParams for non-multipart submission
    const urlParams = new URLSearchParams();
    for (let [key, value] of formData.entries()) {
        console.log(`${key}: ${value}`);
        
        // Convertim data din format d/m/Y în yyyy-MM-dd pentru backend
        if (key === 'travelDate' && value && value.includes('/')) {
            const dateParts = value.split('/');
            if (dateParts.length === 3) {
                const day = dateParts[0].padStart(2, '0');
                const month = dateParts[1].padStart(2, '0');
                const year = dateParts[2];
                const formattedDate = `${year}-${month}-${day}`;
                console.log(`Converting date from ${value} to ${formattedDate}`);
                urlParams.append(key, formattedDate);
                continue;
            }
        }
        
        if (key === 'vehicleId') {
            continue;
        }

        if (!(isPackageOnly && key === 'availableSeats')) {
            urlParams.append(key, value);
        }
    }

    if (resolvedVehicleId && resolvedVehicleId !== '__new__') {
        urlParams.append('vehicleId', resolvedVehicleId);
    }
    
    // Adăugăm câmpul isPackageOnly
    urlParams.append('isPackageOnly', isPackageOnly);
    console.log('isPackageOnly:', isPackageOnly);
    
    // Adăugăm câmpul transportAndPackages bazat pe tipul selectat
    const passengersAndPackagesRadio = document.getElementById('ride-type-passengers-and-packages');
    const transportAndPackages = passengersAndPackagesRadio ? passengersAndPackagesRadio.checked : false;
    urlParams.append('transportAndPackages', transportAndPackages);
    console.log('transportAndPackages:', transportAndPackages);
    
    try {
        const response = await fetch('/api/rides', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: urlParams
        });
        
        console.log('Response status:', response.status);
        
        const data = await response.json();
        console.log('Response data:', data);
        
        if (data.success) {
            showNotification(data.message, 'success');
            setTimeout(() => {
                const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
                window.location.href = '/' + currentLang + '/rides';
            }, 2000);
        } else {
            showNotification(data.message, 'error');
            setSubmitState(false);
        }
    } catch (error) {
        console.error('Eroare la trimiterea datelor:', error);
        showNotification('Eroare la trimiterea datelor. Vă rugăm să încercați din nou.', 'error');
        setSubmitState(false);
    }
}

async function createVehicleFromForm() {
    const makeInput = document.getElementById('vehicle-make');
    const colorInput = document.getElementById('vehicle-color');
    const plateInput = document.getElementById('vehicle-plate');

    if (!makeInput || !colorInput || !plateInput) {
        throw new Error('Vehicle inputs missing');
    }

    const make = makeInput.value.trim();
    const color = colorInput.value.trim();
    const plateNumber = plateInput.value.trim().toUpperCase();

    if (!make || !color || !plateNumber) {
        showNotification(getVehicleText('fillError', 'Completați marca, culoarea și numărul mașinii.'), 'error');
        throw new Error('Vehicle fields missing');
    }

    const response = await fetch('/api/vehicles', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ make, color, plateNumber })
    });

    const data = await response.json();
    if (!response.ok || !data.success) {
        showNotification(data.message || 'Eroare la salvarea vehiculului.', 'error');
        throw new Error('Vehicle create failed');
    }

    return data.vehicle;
}

// Afișarea previzualizării
function showPreview() {
    console.log('Showing preview...');
    
    if (!validateForm()) {
        console.log('Form validation failed');
        return;
    }
    
    const form = document.getElementById('add-ride-form');
    if (!form) {
        console.error('Add ride form not found');
        return;
    }
    
    const formData = new FormData(form);
    currentFormData = Object.fromEntries(formData);
    
    console.log('Form data collected:', currentFormData);
    
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

// Generarea HTML-ului pentru previzualizare
function generatePreviewHTML(data) {
    console.log('Generating preview HTML for data:', data);
    
    const packageRadio = document.getElementById('ride-type-packages');
    const isPackageOnly = packageRadio ? packageRadio.checked : false;
    
    const transportAndPackagesCheckbox = document.getElementById('transport-and-packages');
    const transportAndPackages = transportAndPackagesCheckbox ? transportAndPackagesCheckbox.checked : false;

    const vehicleSelect = document.getElementById('vehicle-select');
    const isNewVehicle = vehicleSelect && vehicleSelect.value === '__new__';
    const vehicleLabel = vehicleSelect && vehicleSelect.value && vehicleSelect.value !== '__new__'
        ? vehicleSelect.options[vehicleSelect.selectedIndex]?.textContent
        : null;
    const makeValue = isNewVehicle ? document.getElementById('vehicle-make')?.value.trim() : '';
    const colorValue = isNewVehicle ? document.getElementById('vehicle-color')?.value.trim() : '';
    const plateValue = isNewVehicle ? document.getElementById('vehicle-plate')?.value.trim() : '';
    const newVehicleLabel = [makeValue, colorValue, plateValue].filter(Boolean).join(' • ');
    
    const vehicleTitle = getVehicleText('label', 'Vehicul');

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
                <p><strong>${vehicleTitle}:</strong> ${vehicleLabel || newVehicleLabel || 'N/A'}</p>
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

// Submit-ul din modal
function submitRide() {
    console.log('Submitting ride from modal...');
    console.log('Current form data:', currentFormData);

    if (isSubmittingRide) {
        console.log('Submission already in progress, ignoring.');
        return;
    }
    
    if (Object.keys(currentFormData).length === 0) {
        showNotification('Nu există date pentru trimitere.', 'error');
        return;
    }
    
    const formData = new FormData();
    Object.entries(currentFormData).forEach(([key, value]) => {
        formData.append(key, value);
    });
    
    submitRideData(formData);
    closeModal();
}

// Setarea datei implicite (gol)
function setDefaultDate() {
    const travelDate = document.getElementById('travel-date');
    if (travelDate) {
        try {
            // Nu setăm nicio dată ca default, lăsăm câmpul gol
            travelDate.value = '';
            console.log('Default date left empty');
        } catch (error) {
            console.error('Error setting default date:', error);
        }
    } else {
        console.warn('Travel date input not found');
    }
}

// Închiderea modalului când se face click în afară
window.addEventListener('click', function(e) {
    const modal = document.getElementById('preview-modal');
    if (e.target === modal) {
        console.log('Modal clicked outside, closing...');
        closeModal();
    }
});

// Închiderea modalului cu ESC
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        console.log('ESC key pressed, closing modal...');
        closeModal();
    }
});

// Închiderea modalului cu butonul X
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-close')) {
        console.log('Modal close button clicked');
        closeModal();
    }
});

// Inițializarea handler-elor pentru tipul de transport
function initializeRideTypeHandlers() {
    console.log('Initializing ride type handlers...');
    
    const passengersOnlyRadio = document.getElementById('ride-type-passengers-only');
    const packagesOnlyRadio = document.getElementById('ride-type-packages-only');
    const passengersAndPackagesRadio = document.getElementById('ride-type-passengers-and-packages');
    const seatsGroup = document.getElementById('seats-group');
    const availableSeatsInput = document.getElementById('available-seats');
    
    if (passengersOnlyRadio && packagesOnlyRadio && passengersAndPackagesRadio && seatsGroup && availableSeatsInput) {
        // Handler pentru transport DOAR pasageri
        passengersOnlyRadio.addEventListener('change', function() {
            if (this.checked) {
                console.log('Passengers only transport selected');
                seatsGroup.style.display = 'block';
                availableSeatsInput.required = true;
                availableSeatsInput.disabled = false;
                availableSeatsInput.value = '1';
            }
        });
        
        // Handler pentru transport DOAR colete
        packagesOnlyRadio.addEventListener('change', function() {
            if (this.checked) {
                console.log('Packages only transport selected');
                seatsGroup.style.display = 'none';
                availableSeatsInput.required = false;
                availableSeatsInput.disabled = true;
                availableSeatsInput.value = '1';
            }
        });
        
        // Handler pentru transport pasageri și colete
        passengersAndPackagesRadio.addEventListener('change', function() {
            if (this.checked) {
                console.log('Passengers and packages transport selected');
                seatsGroup.style.display = 'block';
                availableSeatsInput.required = true;
                availableSeatsInput.disabled = false;
                availableSeatsInput.value = '1';
            }
        });
        
        console.log('Ride type handlers initialized successfully');
    } else {
        console.warn('Some ride type elements not found');
    }
}

function initializeSeatsInput() {
    const availableSeatsInput = document.getElementById('available-seats');
    if (!availableSeatsInput) {
        return;
    }

    availableSeatsInput.addEventListener('input', function() {
        const sanitized = this.value.replace(/[^\d]/g, '').slice(0, 3);
        if (sanitized !== this.value) {
            this.value = sanitized;
        }
    });

    availableSeatsInput.addEventListener('blur', function() {
        if (!this.value) {
            this.value = '1';
            return;
        }

        const seats = parseInt(this.value, 10);
        if (Number.isNaN(seats)) {
            this.value = '1';
            return;
        }

        if (seats < 1) {
            this.value = '1';
        } else if (seats > 100) {
            this.value = '100';
        }
    });

    availableSeatsInput.addEventListener('wheel', function(event) {
        if (document.activeElement === this) {
            event.preventDefault();
        }
    }, { passive: false });
}

async function initializeVehicleHandlers() {
    const vehicleSelect = document.getElementById('vehicle-select');
    const plateInput = document.getElementById('vehicle-plate');

    if (!vehicleSelect) {
        return;
    }

    await loadVehicles();

    vehicleSelect.addEventListener('change', function() {
        toggleVehicleForm(this.value === '__new__');
    });

    if (plateInput) {
        plateInput.addEventListener('input', () => {
            const uppercased = plateInput.value.toUpperCase();
            if (plateInput.value !== uppercased) {
                plateInput.value = uppercased;
            }
        });
    }
}

function getVehicleText(key, fallback) {
    const vehicleSelect = document.getElementById('vehicle-select');
    if (!vehicleSelect) {
        return fallback;
    }
    const value = vehicleSelect.dataset[key];
    return value && value.trim() ? value : fallback;
}

async function loadVehicles() {
    const vehicleSelect = document.getElementById('vehicle-select');
    if (!vehicleSelect) {
        return;
    }

    try {
        const response = await fetch('/api/vehicles');
        if (response.status === 401) {
            renderVehicleOptions([]);
            toggleVehicleForm(true);
            return;
        }

        if (!response.ok) {
            throw new Error('Failed to load vehicles');
        }

        vehiclesCache = await response.json();
        renderVehicleOptions(vehiclesCache);
    } catch (error) {
        console.error('Error loading vehicles:', error);
        renderVehicleOptions([]);
        toggleVehicleForm(true);
    }
}

function renderVehicleOptions(vehicles) {
    const vehicleSelect = document.getElementById('vehicle-select');
    if (!vehicleSelect) {
        return;
    }

    vehicleSelect.innerHTML = '';

    const placeholder = document.createElement('option');
    placeholder.value = '';
    placeholder.textContent = getVehicleText('placeholder', 'Selectați un vehicul');
    placeholder.disabled = true;
    placeholder.selected = true;
    vehicleSelect.appendChild(placeholder);

    if (vehicles && vehicles.length > 0) {
        vehicles.forEach(vehicle => {
            const option = document.createElement('option');
            option.value = vehicle.id;
            option.textContent = `${vehicle.make} • ${vehicle.color} • ${vehicle.plateNumber}`;
            vehicleSelect.appendChild(option);
        });

        const addNew = document.createElement('option');
        addNew.value = '__new__';
        addNew.textContent = getVehicleText('addNew', '+ Adaugă vehicul nou');
        vehicleSelect.appendChild(addNew);

        vehicleSelect.value = String(vehicles[0].id);
        toggleVehicleForm(false);
    } else {
        const addNew = document.createElement('option');
        addNew.value = '__new__';
        addNew.textContent = getVehicleText('addNew', '+ Adaugă vehicul nou');
        vehicleSelect.appendChild(addNew);
        vehicleSelect.value = '';
        toggleVehicleForm(false);
    }
}

function toggleVehicleForm(show) {
    const vehicleForm = document.getElementById('vehicle-form');
    if (!vehicleForm) {
        return;
    }
    vehicleForm.style.display = show ? 'block' : 'none';
}

async function handleSaveVehicle() {
    const makeInput = document.getElementById('vehicle-make');
    const colorInput = document.getElementById('vehicle-color');
    const plateInput = document.getElementById('vehicle-plate');

    if (!makeInput || !colorInput || !plateInput) {
        return;
    }

    const make = makeInput.value.trim();
    const color = colorInput.value.trim();
    const plateNumber = plateInput.value.trim().toUpperCase();

    if (!make || !color || !plateNumber) {
        showNotification(getVehicleText('fillError', 'Completați marca, culoarea și numărul mașinii.'), 'error');
        return;
    }

    try {
        const response = await fetch('/api/vehicles', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                make,
                color,
                plateNumber
            })
        });

        const data = await response.json();
        if (!response.ok || !data.success) {
            showNotification(data.message || 'Eroare la salvarea vehiculului.', 'error');
            return;
        }

        vehiclesCache = [data.vehicle, ...vehiclesCache.filter(v => v.id !== data.vehicle.id)];
        renderVehicleOptions(vehiclesCache);

        const vehicleSelect = document.getElementById('vehicle-select');
        if (vehicleSelect) {
            vehicleSelect.value = data.vehicle.id;
        }

        makeInput.value = '';
        colorInput.value = '';
        plateInput.value = '';
        toggleVehicleForm(false);
        showNotification(getVehicleText('saveSuccess', 'Vehicul salvat și selectat.'), 'success');
    } catch (error) {
        console.error('Error saving vehicle:', error);
        showNotification('Eroare la salvarea vehiculului.', 'error');
    }
}

// Inițializarea calendarului modern cu Flatpickr pentru data și ora
function initializeModernCalendar() {
    // Verificăm dacă Flatpickr este disponibil
    if (typeof flatpickr === 'undefined') {
        console.warn('Flatpickr not loaded, skipping calendar initialization');
        return;
    }
    
    // Calendar pentru data călătoriei
    const travelDateInput = document.getElementById('travel-date');
    if (travelDateInput) {
        try {
            flatpickr(travelDateInput, {
                dateFormat: "d/m/Y",
                locale: "ro",
                minDate: "today",
                maxDate: new Date().fp_incr(365), // Până la un an în viitor
                disableMobile: false,
                allowInput: true,
                clickOpens: true,
                theme: "material_blue",
                placeholder: document.getElementById('travel-date').placeholder || "Selectați data",
                onChange: function(selectedDates, dateStr, instance) {
                    // Actualizăm data implicită când se schimbă
                    if (selectedDates.length > 0) {
                        console.log('Travel date selected:', dateStr);
                    }
                },
                onReady: function(selectedDates, dateStr, instance) {
                    // Forțăm placeholder-ul nostru
                    travelDateInput.placeholder = document.getElementById('travel-date').placeholder || "Selectați data";
                    
                    // Adăugăm iconița de calendar
                    const calendarIcon = document.createElement('i');
                    calendarIcon.className = 'fas fa-calendar-alt calendar-icon';
                    calendarIcon.style.cssText = 'position: absolute; right: 10px; top: 50%; transform: translateY(-50%); color: #10b981; pointer-events: none; z-index: 10;';
                    
                    const inputWrapper = travelDateInput.parentElement;
                    if (inputWrapper) {
                        inputWrapper.style.position = 'relative';
                        inputWrapper.appendChild(calendarIcon);
                    }
                }
            });
            
            console.log('Modern calendar initialized for travel date');
        } catch (error) {
            console.error('Error initializing travel date calendar:', error);
        }
    }
    
    // Time picker pentru ora plecării
    const departureTimeInput = document.getElementById('departure-time');
    if (departureTimeInput) {
        try {
            flatpickr(departureTimeInput, {
                enableTime: true,
                noCalendar: true,
                dateFormat: "H:i",
                time_24hr: true,
                locale: "ro",
                minTime: "00:00",
                maxTime: "23:59",
                disableMobile: false,
                allowInput: true,
                clickOpens: true,
                theme: "material_blue",
                placeholder: document.getElementById('departure-time').placeholder || "Selectați ora", // Folosim placeholder-ul din HTML sau default
                onChange: function(selectedDates, timeStr, instance) {
                    if (selectedDates.length > 0) {
                        console.log('Departure time selected:', timeStr);
                    }
                },
                onReady: function(selectedDates, timeStr, instance) {
                    // Forțăm placeholder-ul nostru
                    departureTimeInput.placeholder = document.getElementById('departure-time').placeholder || "Selectați ora";
                    
                    // Adăugăm iconița de ceas
                    const clockIcon = document.createElement('i');
                    clockIcon.className = 'fas fa-clock calendar-icon';
                    clockIcon.style.cssText = 'position: absolute; right: 10px; top: 50%; transform: translateY(-50%); color: #10b981; pointer-events: none; z-index: 10;';
                    
                    const inputWrapper = departureTimeInput.parentElement;
                    if (inputWrapper) {
                        inputWrapper.style.position = 'relative';
                        inputWrapper.appendChild(clockIcon);
                    }
                }
            });
            
            console.log('Modern time picker initialized for departure time');
        } catch (error) {
            console.error('Error initializing departure time picker:', error);
        }
    }
}
