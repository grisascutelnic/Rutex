// Funcționalitate pentru pagina rides
// Event listener pentru încărcarea paginii
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 DOM Content Loaded - Initializing rides page...');
    
    // Inițializăm pagina
    initializeRidesPage();
    
    // Forțăm aplicarea stilurilor corecte pentru results-info după o mică întârziere
    setTimeout(() => {
        forceCorrectResultsInfoStyles();
    }, 100);
});

// Inițializarea paginii rides
function initializeRidesPage() {
    console.log('🚀 Initializing rides page...');
    
    // Inițializăm autocomplete pentru filtre
    initializeFilterAutocomplete();
    
    // Adăugăm event listeners pentru butoane
    initializeRideActions();
    
    // Adăugăm event listener pentru filtrare
    initializeFiltering();
    
    // Inițializăm Flatpickr pentru input-ul de dată
    initializeDatePicker();
    
    // Inițializăm checkbox-ul de colete ÎNAINTE de încărcarea parametrilor din URL
    console.log('📦 About to initialize packages checkbox...');
    initializePackagesCheckbox();
    
    // Apoi încărcăm parametrii din URL
    loadSearchParamsFromURL();
    
    // Nu mai încărcăm toate cursele la inițializare pentru a păstra HTML-ul original cu onclick
    console.log('🔧 Keeping original Thymeleaf HTML with onclick handlers');
}

// Funcție pentru forțarea stilurilor corecte pentru results-info
function forceCorrectResultsInfoStyles() {
    console.log('🎨 Forcing correct styles for results-info...');
    const resultsInfo = document.querySelector('.results-info');
    if (resultsInfo) {
        // Aplicăm stilurile corecte la secțiunea existentă
        resultsInfo.setAttribute('style', 'background-color: transparent !important; padding: 0.2rem 0 !important; border-bottom: none !important; position: relative !important; box-shadow: none !important; margin: 0.4rem 0 0.2rem !important;');
        
        // Aplicăm stilurile corecte la container
        const container = resultsInfo.querySelector('.container');
        if (container) {
            container.setAttribute('style', 'display: flex !important; justify-content: center !important; align-items: center !important;');
        }
        
        // Aplicăm stilurile corecte la paragraf
        const paragraph = resultsInfo.querySelector('p');
        if (paragraph) {
            paragraph.setAttribute('style', 'margin: 0 !important; color: #065f46 !important; font-size: 1rem !important; font-weight: 600 !important; text-align: center !important; position: relative !important; z-index: 1 !important; letter-spacing: 0.025em !important; background: rgba(255, 255, 255, 0.9) !important; padding: 0.75rem 1.5rem !important; border-radius: 25px !important; box-shadow: 0 6px 18px rgba(16, 185, 129, 0.12) !important; border: 1px solid rgba(209, 250, 229, 0.9) !important;');
        }
        
        // Aplicăm stilurile corecte la strong
        const strongElement = resultsInfo.querySelector('strong');
        if (strongElement) {
            strongElement.setAttribute('style', 'color: #10b981 !important; font-weight: 700 !important; font-size: 1.1rem !important; margin-left: 0.25rem !important;');
        }
        
        console.log('✅ Applied correct styles to existing results-info section');
    } else {
        console.log('📦 No results-info section found to style');
    }
}

// Inițializarea autocomplete pentru filtre
function initializeFilterAutocomplete() {
    console.log('Initializing filter autocomplete with Google Places API...');
    
    // Verificăm dacă elementele de filtrare există
    const filterFrom = document.getElementById('filter-from');
    const filterTo = document.getElementById('filter-to');
    
    if (!filterFrom || !filterTo) {
        console.log('Filter elements not found, skipping autocomplete initialization');
        return;
    }
    
    // Inițializăm autocomplete pentru input-ul "from"
    const fromAutocomplete = new LocalityAutocomplete({
        inputSelector: '#filter-from',
        resultsContainerSelector: '#filter-from-suggestions',
        language: 'ro',
        limit: 10,
        includeDistrict: true
    });
    
    // Inițializăm autocomplete pentru input-ul "to"
    const toAutocomplete = new LocalityAutocomplete({
        inputSelector: '#filter-to',
        resultsContainerSelector: '#filter-to-suggestions',
        language: 'ro',
        limit: 10,
        includeDistrict: true
    });
    
    console.log('Filter autocomplete initialized successfully with Google Places API');
}

// Inițializarea acțiunilor pentru curse
function initializeRideActions() {
    console.log('🔧 Initializing ride actions...');
    
        // Test pentru a verifica dacă există containere ride-card
    const rideCards = document.querySelectorAll('.ride-card.clickable');
    console.log('🔍 Found', rideCards.length, 'ride cards on page');
    rideCards.forEach((card, index) => {
        const rideId = card.getAttribute('data-ride-id');
        const onclick = card.getAttribute('onclick');
        console.log(`🔍 Ride card ${index}: ID=${rideId}, onclick=${onclick}`);
        
        // Verificăm dacă onclick-ul este setat corect
        if (!onclick || !onclick.includes('showRideDetails')) {
            console.warn(`⚠️ Ride card ${index} missing onclick handler`);
        }
    });
    
    // Nu mai folosim event delegation pentru că folosim onclick inline ca pe index.html
    console.log('🔧 Using onclick inline approach like index.html');
    }

// Inițializarea filtrarei
function initializeFiltering() {
    // Adăugăm event listener pentru formularul de căutare
    const searchForm = document.querySelector('.search-form');
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            // Prevenim trimiterea formularului HTML și folosim JavaScript pentru căutare
            e.preventDefault();
            applyFilters();
        });
    }
    
    // Event listener pentru butonul de căutare
    const searchBtn = document.querySelector('.search-btn');
    if (searchBtn) {
        searchBtn.addEventListener('click', function(e) {
            e.preventDefault();
            applyFilters();
        });
    }
    
    // Event listener-ul pentru packages a fost mutat în initializePackagesCheckbox()
}



// Afișarea detaliilor cursei
function showRideDetails(rideId) {
    console.log('🔍 showRideDetails called with rideId:', rideId);
    
    if (!rideId || rideId <= 0) {
        console.error('❌ Invalid ride ID:', rideId);
        return;
    }
    
    // Detectăm limba din URL sau din elementul de limbă
    let currentLang = 'ro'; // default
    
    // Încercăm să detectăm din URL
    const path = window.location.pathname;
    if (path.startsWith('/ru/') || path === '/ru') {
        currentLang = 'ru';
    } else if (path.startsWith('/ro/') || path === '/ro') {
        currentLang = 'ro';
    } else {
        // Fallback: detectăm din elementul de limbă
        const currentLangElement = document.querySelector('.current-lang');
        if (currentLangElement) {
            currentLang = currentLangElement.textContent === 'RO' ? 'ro' : 'ru';
        }
    }
    
    const url = '/' + currentLang + '/ride/' + rideId;
    console.log('🌐 Redirecting to:', url);
    console.log('🌐 Full URL:', window.location.origin + url);
    
    // Verificăm dacă URL-ul este valid înainte de redirecționare
    if (url && url.length > 0) {
        window.location.href = url;
    } else {
        console.error('❌ Invalid URL generated:', url);
    }
}

// Navigarea la profilul utilizatorului
function navigateToUserProfile(userId) {
    console.log('🧭 navigateToUserProfile called in rides.js with userId:', userId);
    
    if (userId) {
        const profileUrl = `/profile/${userId}`;
        console.log('🌐 Redirecting to profile from rides.js:', profileUrl);
        window.location.href = profileUrl;
    } else {
        console.error('❌ User ID is missing for profile navigation in rides.js.');
        showNotification('Eroare: ID-ul utilizatorului nu a fost găsit.', 'error');
    }
}

// Aplicarea filtrelor
function applyFilters() {
    console.log('🔍 applyFilters called');
    
    const fromLocation = document.getElementById('filter-from')?.value || '';
    const toLocation = document.getElementById('filter-to')?.value || '';
    const travelDate = document.getElementById('filter-date')?.value || '';
    const packages = document.getElementById('filter-packages')?.value || '';
    
    console.log('🔍 Form values:', { fromLocation, toLocation, travelDate, packages });
    
    // Traducem orașele în română pentru căutare în backend
    const translatedFromLocation = window.CityTranslations ? 
        window.CityTranslations.reverseTranslateCity(fromLocation) : fromLocation;
    const translatedToLocation = window.CityTranslations ? 
        window.CityTranslations.reverseTranslateCity(toLocation) : toLocation;
    
    // Convertim data din format d/m/Y în yyyy-MM-dd pentru backend
    let formattedDate = travelDate;
    console.log('🔍 Original travelDate:', travelDate);
    
    if (travelDate && travelDate.trim() !== '' && travelDate.includes('/')) {
        const dateParts = travelDate.split('/');
        console.log('🔍 Date parts:', dateParts);
        if (dateParts.length === 3) {
            const day = dateParts[0].padStart(2, '0');
            const month = dateParts[1].padStart(2, '0');
            const year = dateParts[2];
            formattedDate = `${year}-${month}-${day}`;
            console.log('🔍 Converted date parts:', { day, month, year });
        }
    } else if (!travelDate || travelDate.trim() === '') {
        formattedDate = ''; // Nu trimitem data dacă este goală
        console.log('🔍 Empty date, not sending to backend');
    }
    
    console.log('🔍 Final formatted date:', formattedDate);
    
    // Construim URL-ul pentru căutare cu parametrii pentru backend
    const params = new URLSearchParams();
    if (translatedFromLocation) params.append('from', translatedFromLocation);
    if (translatedToLocation) params.append('to', translatedToLocation);
    if (formattedDate && formattedDate.trim() !== '') params.append('date', formattedDate);
    
    // Corectăm logica pentru packages - trimitem doar dacă este 'on'
    if (packages === 'on') {
        params.append('packages', 'on');
        console.log('📦 Adding packages=on to search params');
    } else {
        console.log('📦 Packages not selected, not adding to search params');
    }
    
    // Resetăm la pagina 1 pentru căutări noi
    params.append('page', '0');
    
    console.log('🔍 Search params:', params.toString());
    
    // Redirecționăm către pagina rides cu parametrii de filtrare
    // Astfel backend-ul va face paginarea corectă
    const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
    const ridesUrl = `/${currentLang}/rides?${params.toString()}`;
    
    console.log('🔍 Redirecting to:', ridesUrl);
    
    // Păstrăm valorile din formular înainte de redirecționare
    preserveFormValues(fromLocation, toLocation, travelDate, packages);
    
    window.location.href = ridesUrl;
}

// Funcție pentru păstrarea valorilor din formular după căutare
function preserveFormValues(fromLocation, toLocation, travelDate, packages) {
    console.log('💾 preserveFormValues called with:', { fromLocation, toLocation, travelDate, packages });
    
    // Păstrăm valorile în input-uri
    const fromInput = document.getElementById('filter-from');
    const toInput = document.getElementById('filter-to');
    const dateInput = document.getElementById('filter-date');
    const packagesInput = document.getElementById('filter-packages');
    
    if (fromInput) fromInput.value = fromLocation;
    if (toInput) toInput.value = toLocation;
    
    // Setăm data în Flatpickr dacă există o valoare specifică
    if (dateInput && travelDate && travelDate.trim() !== '') {
        console.log('💾 Setting date:', travelDate);
        // Verificăm dacă input-ul are o instanță Flatpickr
        if (dateInput._flatpickr) {
            console.log('💾 Using Flatpickr instance to set date');
            dateInput._flatpickr.setDate(travelDate);
        } else {
            console.log('💾 Setting date directly in input');
            dateInput.value = travelDate;
        }
    } else if (dateInput && (!travelDate || travelDate.trim() === '')) {
        console.log('💾 Clearing date input');
        if (dateInput._flatpickr) {
            dateInput._flatpickr.clear();
        } else {
            dateInput.value = '';
        }
    }
    
    // NU păstrăm valoarea packages în preserveFormValues pentru a evita conflictele
    // Valoarea packages va fi gestionată doar de event listener-ul checkbox-ului
    console.log('💾 Skipping packages value preservation to avoid conflicts');
    
    // Păstrăm și valoarea packages
    if (packagesInput) {
        packagesInput.value = packages ? 'on' : '';
        console.log('💾 Set packages value:', packagesInput.value);
    }
}

// Inițializarea Flatpickr pentru input-ul de dată
function initializeDatePicker() {
    console.log('🔧 initializeDatePicker called');
    const dateInput = document.getElementById('filter-date');
    console.log('🔧 Date input found:', !!dateInput);
    console.log('🔧 Flatpickr available:', typeof flatpickr !== 'undefined');
    
    if (dateInput && typeof flatpickr !== 'undefined') {
        try {
            // Verificăm dacă input-ul are deja o instanță Flatpickr
            if (dateInput._flatpickr) {
                console.log('🔧 Input already has Flatpickr instance, destroying it');
                dateInput._flatpickr.destroy();
            }
            
            flatpickr(dateInput, {
                dateFormat: "d/m/Y",
                locale: "ro",
                minDate: "today",
                maxDate: new Date().fp_incr(365), // Până la un an în viitor
                disableMobile: "true", // Dezactivăm iconițele mobile
                allowInput: true,
                clickOpens: true,
                theme: "light", // Folosim un theme simplu fără iconițe
                wrap: false, // Nu înfășoară input-ul pentru a evita iconițele duplicate
                placeholder: document.getElementById('filter-date').placeholder || "Selectați data", // Folosim placeholder-ul din HTML sau default
                onChange: function(selectedDates, dateStr, instance) {
                    console.log('Date selected:', dateStr);
                    // Nu aplicăm filtrele automat pentru a evita apeluri multiple
                },
                onReady: function(selectedDates, dateStr, instance) {
                    console.log('🔧 Flatpickr ready, leaving field empty');
                    // Forțăm placeholder-ul nostru
                    dateInput.placeholder = document.getElementById('filter-date').placeholder || "Selectați data";
                    
                    // Curățăm orice elemente suplimentare adăugate de Flatpickr
                    setTimeout(() => {
                        const container = dateInput.parentElement;
                        const flatpickrElements = container.querySelectorAll('[class*="flatpickr"]:not(.flatpickr-input)');
                        flatpickrElements.forEach(element => {
                            console.log('Removing Flatpickr element:', element);
                            element.remove();
                        });
                    }, 10);
                }
            });
            console.log('✅ Flatpickr initialized for filter-date input');
        } catch (error) {
            console.error('❌ Error initializing Flatpickr for filter-date:', error);
        }
    } else {
        console.warn('⚠️ Flatpickr not available or filter-date input not found');
    }
}

// Funcție pentru încărcarea parametrilor de căutare din URL
function loadSearchParamsFromURL() {
    const urlParams = new URLSearchParams(window.location.search);
    const fromLocation = urlParams.get('fromLocation');
    const toLocation = urlParams.get('toLocation');
    const travelDate = urlParams.get('travelDate');
    const packages = urlParams.get('packages') === 'on';
    
    // Setăm starea inițială pentru packages
    packagesChecked = packages;
    
    // Convertim data din format yyyy-MM-dd în d/m/Y pentru afișare
    let displayDate = travelDate;
    if (travelDate && travelDate.includes('-')) {
        const dateParts = travelDate.split('-');
        if (dateParts.length === 3) {
            const year = dateParts[0];
            const month = dateParts[1];
            const day = dateParts[2];
            displayDate = `${day}/${month}/${year}`;
        }
    }
    
    // Dacă există parametri de căutare în URL, aplicăm filtrele
    if (fromLocation || toLocation || travelDate || packages) {
        console.log('Loading search params from URL:', { fromLocation, toLocation, travelDate: displayDate, packages });
        
        // Setăm valorile în formular (fără packages - va fi gestionat separat)
        preserveFormValues(fromLocation || '', toLocation || '', displayDate || '', '');
        
        // Setăm valoarea packages separat pentru a evita conflictele
        const packagesInput = document.getElementById('filter-packages');
        if (packagesInput) {
            packagesInput.value = packages ? 'on' : '';
            console.log('📦 Set packages value from URL:', packagesInput.value);
        }
        
        // Aplicăm filtrele automat doar dacă avem parametri de căutare reali (nu doar packages=on)
        if (fromLocation || toLocation || travelDate) {
            // TEMPORAR: Comentez apelul automat pentru a păstra paginarea din backend
            // setTimeout(() => {
            //     applyFilters();
            // }, 100);
            console.log('🔧 Skipping automatic filter application to preserve backend pagination');
        } else {
            console.log('📦 Only packages parameter found, not applying filters automatically');
        }
    } else {
        // Dacă nu există parametri, aplicăm filtrele cu starea inițială (fără packages)
        // TEMPORAR: Comentez apelul automat pentru a păstra paginarea din backend
        // setTimeout(() => {
        //     applyFilters();
        // }, 100);
        console.log('🔧 No URL parameters found, skipping automatic filter application to preserve backend pagination');
    }
}

// Funcție globală pentru traducere
function translateText(key, defaultText) {
    const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
    if (currentLang === 'ro') {
        return defaultText;
    }
    // Pentru moment, returnăm textul implicit
    // În viitor, putem încărca traducerile din backend
    const translations = {
        'rides.details': 'Детали',
        'rides.available_seats': 'мест доступно',
        'rides.per_seat': 'за место',
        'rides.no_rides': 'Нет доступных поездок',
        'rides.no_rides_message': 'Попробуйте изменить фильтры или вернуться позже.',
        'rides.error_loading': 'Ошибка загрузки поездок',
        'rides.retry': 'Повторить',
        'rides.found_rides': 'Найдено {count} поездок',
        'rides.available_seats_text': 'мест',
        'rides.package_only': 'Транспортирую только посылки',
        'rides.transport_and_packages': 'Транспортирую и посылки',
        'rides.views': 'Просмотры'
    };
    return translations[key] || defaultText;
}

// Actualizarea listei de curse
function updateRidesList(rides) {
    console.log('Updating rides list with:', rides);
    
    const ridesList = document.getElementById('rides-list');
    console.log('Found rides list element:', ridesList);
    
    if (!ridesList) {
        console.error('Element rides-list not found in DOM');
        return;
    }
    
    // Actualizăm secțiunea "Găsite x curse"
    updateResultsInfo(rides ? rides.length : 0);
    
    if (!rides || rides.length === 0) {
        console.log('No rides to display, showing empty state');
        ridesList.innerHTML = `
            <div class="no-rides">
                <i class="fas fa-search"></i>
                <h3>${translateText('rides.no_rides', 'Nu sunt curse disponibile')}</h3>
                <p>${translateText('rides.no_rides_message', 'Încearcă să modifici filtrele sau să revii mai târziu.')}</p>
            </div>
        `;
        
        // Scroll automat la "Nu sunt curse disponibile"
        setTimeout(() => {
            const noRidesElement = document.querySelector('.no-rides');
            if (noRidesElement) {
                noRidesElement.scrollIntoView({ 
                    behavior: 'smooth', 
                    block: 'center',
                    inline: 'nearest'
                });
                console.log('Scrolled to no rides section');
            }
        }, 100);
        
        return;
    }
    
    console.log('Generating HTML for', rides.length, 'rides');
    const ridesHTML = rides.map(ride => generateRideCardHTML(ride)).join('');
    console.log('Generated HTML length:', ridesHTML.length);
    
    ridesList.innerHTML = ridesHTML;
    console.log('Updated rides list HTML');
    
    // Scroll automat la secțiunea "Găsite x curse" după căutare
    setTimeout(() => {
        const resultsInfo = document.querySelector('.results-info');
        if (resultsInfo) {
            resultsInfo.scrollIntoView({ 
                behavior: 'smooth', 
                block: 'center',
                inline: 'nearest'
            });
            console.log('Scrolled to results info section');
        }
    }, 100); // Mic delay pentru a permite renderizarea
}

// Funcție pentru actualizarea secțiunii "Găsite x curse"
function updateResultsInfo(count) {
    console.log('Updating results info with count:', count);
    
    // Căutăm secțiunea existentă (poate fi creată de Thymeleaf sau JavaScript)
    let resultsInfo = document.querySelector('.results-info');
    
    if (count > 0) {
        // Dacă avem rezultate, creăm sau actualizăm secțiunea
        if (!resultsInfo) {
            // Creăm secțiunea dacă nu există
            const searchSection = document.querySelector('.search-section');
            if (searchSection) {
                resultsInfo = document.createElement('section');
                resultsInfo.className = 'results-info';
                // Get translation for "found rides"
                const foundRidesText = translateText('rides.found_rides', `Găsite ${count} curse`).replace('{count}', count);
                resultsInfo.innerHTML = `
                    <div class="container">
                        <p>${foundRidesText}</p>
                    </div>
                `;
                // Inserăm după secțiunea de căutare
                searchSection.parentNode.insertBefore(resultsInfo, searchSection.nextSibling);
                console.log('Created results info section with count:', count);
            } else {
                console.warn('Search section not found, cannot create results info');
            }
        } else {
            // Pentru a ne asigura că CSS-ul se aplică corect, eliminăm și recreăm secțiunea
            console.log('Results info section exists, recreating to ensure proper CSS...');
            const searchSection = document.querySelector('.search-section');
            if (searchSection) {
                // Eliminăm secțiunea existentă
                resultsInfo.remove();
                
                // Creăm o nouă secțiune cu stilurile corecte
                resultsInfo = document.createElement('section');
                resultsInfo.className = 'results-info';
                // Get translation for "found rides"
                const foundRidesText = translateText('rides.found_rides', `Găsite ${count} curse`).replace('{count}', count);
                resultsInfo.innerHTML = `
                    <div class="container">
                        <p>${foundRidesText}</p>
                    </div>
                `;
                // Inserăm după secțiunea de căutare
                searchSection.parentNode.insertBefore(resultsInfo, searchSection.nextSibling);
                console.log('Recreated results info section with count:', count);
            }
        }
    } else {
        // Dacă nu avem rezultate, ștergem secțiunea
        if (resultsInfo) {
            resultsInfo.remove();
            console.log('Removed results info section (no results)');
        }
    }
}

// Generarea HTML-ului pentru o carte de cursă
function generateRideCardHTML(ride) {
    console.log('Generating HTML for ride:', ride);
    
    // Get current language for date formatting
    const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
    const locale = currentLang === 'ru' ? 'ru-RU' : 'ro-RO';
    
    const travelDate = new Date(ride.travelDate).toLocaleDateString(locale, {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
    });
    
    const departureTime = new Date(ride.departureTime).toLocaleTimeString(locale, {
        hour: '2-digit',
        minute: '2-digit'
    });
    
    console.log('Formatted travel date:', travelDate);
    console.log('Formatted departure time:', departureTime);
    
    // Folosim exact același format ca pe index.html pentru localități
    const fromLocationTitle = ride.fromLocation.includes(',') ? 
        ride.fromLocation.substring(0, ride.fromLocation.indexOf(',')) : ride.fromLocation;
    const fromLocationSubtitle = ride.fromLocation.includes(',') ? 
        ride.fromLocation.substring(ride.fromLocation.indexOf(',') + 1).trim() : '';
    
    const toLocationTitle = ride.toLocation.includes(',') ? 
        ride.toLocation.substring(0, ride.toLocation.indexOf(',')) : ride.toLocation;
    const toLocationSubtitle = ride.toLocation.includes(',') ? 
        ride.toLocation.substring(ride.toLocation.indexOf(',') + 1).trim() : '';
    
    const html = `
        <div class="ride-card clickable" data-ride-id="${ride.id}" onclick="showRideDetails(${ride.id})" style="cursor: pointer;">
            <div class="ride-header">
                <div class="ride-route">
                    <div class="route-point">
                        <i class="fas fa-map-marker-alt"></i>
                        <div class="location-info">
                            <span class="location-title" data-original-text="${ride.fromLocation}">${fromLocationTitle}</span>
                            ${fromLocationSubtitle ? `<span class="location-subtitle">${fromLocationSubtitle}</span>` : ''}
                        </div>
                    </div>
                    <div class="route-arrow">
                        <i class="fas fa-arrow-right"></i>
                    </div>
                    <div class="route-point">
                        <i class="fas fa-map-marker-alt"></i>
                        <div class="location-info">
                            <span class="location-title" data-original-text="${ride.toLocation}">${toLocationTitle}</span>
                            ${toLocationSubtitle ? `<span class="location-subtitle">${toLocationSubtitle}</span>` : ''}
                        </div>
                    </div>
                </div>
            </div>
            <div class="ride-details">
                <div class="ride-info">
                    <div class="info-item">
                        <i class="fas fa-calendar"></i>
                        <span>${travelDate}</span>
                    </div>
                    <div class="info-item">
                        <i class="fas fa-clock"></i>
                        <span>${departureTime}</span>
                    </div>
                    ${ride.isPackageOnly ? `
                        <div class="info-item package-item">
                            <i class="fas fa-box"></i>
                            <span>${translateText('rides.package_only', 'Transport doar colete')}</span>
                        </div>
                    ` : `
                        <div class="info-item">
                            <i class="fas fa-users"></i>
                            <span>${ride.availableSeats || 0} ${translateText('rides.available_seats_text', 'locuri')}</span>
                        </div>
                    `}
                    ${!ride.isPackageOnly && ride.transportAndPackages ? `
                        <div class="info-item transport-packages-item">
                            <i class="fas fa-box"></i>
                            <span>${translateText('rides.transport_and_packages', 'Transport și colete')}</span>
                        </div>
                    ` : ''}
                    <div class="info-item">
                        <i class="fas fa-user"></i>
                        <span class="driver-name">${ride.driverName || 'Nume șofer'}</span>
                    </div>
                </div>

            </div>
            ${ride.description ? `
                <div class="ride-description">
                    <p>${ride.description}</p>
                </div>
            ` : ''}
            
            <!-- View count indicator -->
            <div class="view-count-indicator">
                <i class="fas fa-eye"></i>
                <span>${translateText('rides.views', 'Vizualizări')}: <span>${ride.viewCount || 0}</span></span>
            </div>
        </div>
    `;
    
    console.log('Generated HTML for ride', ride.id, ':', html.substring(0, 100) + '...');
    return html;
}


// Încărcarea tuturor curselor disponibile
function loadAllRides() {
    console.log('Starting to load all rides...');
    
    // Verificăm dacă elementul rides-list există
    const ridesList = document.getElementById('rides-list');
    if (!ridesList) {
        console.error('Element rides-list not found in DOM');
        return;
    }
    
    // Mai întâi testăm conexiunea la baza de date
    fetch('/api/rides/test')
        .then(response => response.json())
        .then(testData => {
            console.log('Database test result:', testData);
            if (!testData.success) {
                throw new Error('Database connection failed: ' + testData.message);
            }
            
            // Dacă testul a reușit, încărcăm cursele
            return fetch('/api/rides');
        })
        .then(response => {
            console.log('Response status:', response.status);
            console.log('Response headers:', response.headers);
            if (!response.ok) {
                throw new Error(`Network response was not ok: ${response.status} ${response.statusText}`);
            }
            return response.json();
        })
        .then(rides => {
            console.log('Successfully loaded rides:', rides);
            console.log('Number of rides:', rides.length);
            
            if (!rides || rides.length === 0) {
                // Nu sunt curse disponibile
                ridesList.innerHTML = `
                    <div class="no-rides">
                        <i class="fas fa-search"></i>
                        <h3>${translateText('rides.no_rides', 'Nu sunt curse disponibile')}</h3>
                        <p>${translateText('rides.no_rides_message', 'Încearcă să modifici filtrele sau să revii mai târziu.')}</p>
                    </div>
                `;
            } else {
                updateRidesList(rides);
            }
        })
        .catch(error => {
            console.error('Error loading rides:', error);
            showNotification('Eroare la încărcarea curselor: ' + error.message, 'error');
            // Afișăm mesajul de eroare în lista de curse
            ridesList.innerHTML = `
                <div class="no-rides">
                    <i class="fas fa-exclamation-triangle"></i>
                    <h3>${translateText('rides.error_loading', 'Eroare la încărcarea curselor')}</h3>
                    <p>${error.message}</p>
                    <p>Vă rugăm să reîncercați mai târziu.</p>
                    <button onclick="loadAllRides()" class="btn-retry">
                        <i class="fas fa-redo"></i>
                        ${translateText('rides.retry', 'Reîncearcă')}
                    </button>
                </div>
            `;
        });
}

// Funcția pentru toggle checkbox-ului de colete - ELIMINATĂ pentru a evita conflictele
// Checkbox-ul este acum gestionat direct de event listener-urile din initializePackagesCheckbox()

// Funcția preventFormSubmitOnCheckbox a fost eliminată pentru a evita conflictele
// Checkbox-ul este acum gestionat direct de event listener-urile din initializePackagesCheckbox()

// Inițializarea stilurilor pentru checkbox-ul de colete
function initializePackagesCheckbox() {
    console.log('Initializing packages checkbox...');
    
    // Event listener pentru container-ul de colete - exact ca data
    const packagesContainer = document.getElementById('packages-container');
    const packagesInput = document.getElementById('filter-packages');
    
    if (packagesContainer && packagesInput) {
        // Verificăm starea din URL înainte de a seta starea inițială
        const urlParams = new URLSearchParams(window.location.search);
        const urlPackages = urlParams.get('packages') === 'on';
        
        // Verificăm starea inițială și aplicăm stilurile corespunzătoare
        const initialValue = packagesInput.value || (urlPackages ? 'on' : '');
        console.log('📦 Initial packages value:', initialValue, 'URL packages:', urlPackages);
        
        // Setăm valoarea corectă în input
        if (urlPackages && !packagesInput.value) {
            packagesInput.value = 'on';
        }
        
        if (packagesInput.value === 'on' || urlPackages) {
            packagesContainer.classList.add('checked');
            console.log('✅ Set initial state: checked');
        } else {
            packagesContainer.classList.remove('checked');
            console.log('❌ Set initial state: unchecked');
        }
        
        // Eliminăm event listener-ul vechi dacă există
        const oldClickHandler = packagesContainer._clickHandler;
        if (oldClickHandler) {
            packagesContainer.removeEventListener('click', oldClickHandler);
            console.log('📦 Removed old click handler');
        }
        
        // Creăm noul event listener
        const clickHandler = function(e) {
            e.preventDefault(); // Previne submit-ul formularului
            e.stopPropagation(); // Previne propagarea event-ului
            
            console.log('🔄 Packages checkbox clicked');
            
            // Toggle starea - exact ca data
            const currentValue = packagesInput.value;
            const newValue = currentValue === 'on' ? '' : 'on';
            packagesInput.value = newValue;
            
            console.log('📦 Current value:', currentValue, '→ New value:', newValue);
            
            // Aplicăm stilurile
            if (newValue === 'on') {
                packagesContainer.classList.add('checked');
                console.log('✅ Packages checkbox checked');
            } else {
                packagesContainer.classList.remove('checked');
                console.log('❌ Packages checkbox unchecked');
            }
            
            // NU aplicăm filtrele automat - exact ca data
            // Filtrele se aplică doar când se apasă butonul "Caută"
        };
        
        // Salvăm referința la handler pentru a putea să îl eliminăm mai târziu
        packagesContainer._clickHandler = clickHandler;
        
        // Adăugăm noul event listener
        packagesContainer.addEventListener('click', clickHandler);
        
        console.log('✅ Packages checkbox initialized successfully');
    } else {
        console.error('❌ Packages checkbox elements not found:', { packagesContainer, packagesInput });
    }
}
