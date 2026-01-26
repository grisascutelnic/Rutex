// Variabile globale
let passengersCount = 1;
let luggageCount = 0;

// Inițializare când se încarcă pagina
document.addEventListener('DOMContentLoaded', function() {
    console.log('Main page loaded, initializing...');
    
    window.addEventListener('load', function() {
        initializeBackgroundCars();
    });

    // Verificăm dacă suntem pe pagina rides
    const isRidesPage = window.location.pathname.includes('/rides');
    
    if (isRidesPage) {
        console.log('🚗 Rides page detected, skipping main page functions');
        // Pe pagina rides, doar inițializăm funcțiile necesare
        initializeHamburgerMenu();
        initializeAddRideButton();
        initializeRideDateFormats();
        initializePhoneCopyButtons();
        saveCurrentUrlForRedirect();
    } else {
        console.log('🏠 Main page detected, initializing all functions');
        // Pe pagina principală, inițializăm toate funcțiile
        initializeCounters();
        initializeSearchForm();
        initializeLocationAutocomplete();
        initializeHamburgerMenu();
        initializeAddRideButton();
        initializeRidesPageAutocomplete();
        initializeModernCalendar();
        initializeUserProfileLinks();
        initializeRideDateFormats();
        initializePhoneCopyButtons();
        saveCurrentUrlForRedirect();
    }
});

function initializeBackgroundCars() {
    if (document.querySelector('.bg-cars')) {
        return;
    }

    const layer = document.createElement('div');
    layer.className = 'bg-cars';
    document.body.appendChild(layer);

    const isMobile = window.matchMedia('(max-width: 768px)').matches;
    const carCount = isMobile ? 2 : 4;
    const carSize = isMobile ? 120 : 190;
    const minOpacity = 0.25;
    const maxOpacity = 0.5;
    const speedMin = isMobile ? 70 : 140;
    const speedMax = isMobile ? 110 : 200;
    const spacingFactor = 3.2;

    const assets = {
        left: '/img/masina_stanga.gif',
        right: '/img/masina_dreapta.gif'
    };

    const lanes = buildLanes(carCount, 10, 90);

    for (let i = 0; i < carCount; i++) {
        const car = document.createElement('span');
        const size = carSize;
        const offset = size * 1.1;
        const distance = window.innerWidth + (offset * 2);
        const speed = randomBetween(speedMin, speedMax);
        const duration = distance / speed;
        const cycle = duration * spacingFactor;
        const jitter = randomBetween(0, duration * 0.4);
        const delay = -(i / carCount) * cycle - jitter;
        const lane = lanes[i % lanes.length];
        const opacity = randomBetween(minOpacity, maxOpacity);
        const movingRight = i % 2 === 0;

        car.className = 'bg-car bg-car--x';
        car.style.setProperty('--car-size', `${size}px`);
        car.style.setProperty('--car-duration', `${duration}s`);
        car.style.setProperty('--car-opacity', opacity.toFixed(2));
        car.style.setProperty('animation-delay', `${delay}s`);

        const fromX = movingRight ? `-${offset}px` : `calc(100vw + ${offset}px)`;
        const toX = movingRight ? `calc(100vw + ${offset}px)` : `-${offset}px`;

        car.style.setProperty('--from-x', fromX);
        car.style.setProperty('--to-x', toX);
        car.style.setProperty('--from-y', `${lane}vh`);
        car.style.setProperty('--to-y', `${lane}vh`);
        car.style.backgroundImage = `url('${movingRight ? assets.right : assets.left}')`;

        layer.appendChild(car);
    }
}

function randomBetween(min, max) {
    return Math.random() * (max - min) + min;
}

function buildLanes(count, min, max) {
    const gap = (max - min) / Math.max(count, 1);
    const lanes = [];
    for (let i = 0; i < count; i++) {
        lanes.push(min + gap * i + gap * 0.5);
    }
    for (let i = lanes.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        const temp = lanes[i];
        lanes[i] = lanes[j];
        lanes[j] = temp;
    }
    return lanes;
}

/**
 * Salvează URL-ul curent pentru redirecționare după logare/înregistrare
 * Nu salvează URL-urile de login/register pentru a evita bucle
 */
function saveCurrentUrlForRedirect() {
    const currentUrl = window.location.pathname + window.location.search;
    const isLoginPage = currentUrl.includes('/login');
    const isRegisterPage = currentUrl.includes('/register');
    const isLogoutPage = currentUrl.includes('/logout');
    
    // Nu salva URL-ul dacă suntem pe paginile de autentificare
    if (!isLoginPage && !isRegisterPage && !isLogoutPage) {
        sessionStorage.setItem('redirectAfterLogin', currentUrl);
        console.log('🔗 Saved current URL for redirect:', currentUrl);
    }
}

// Inițializarea contoarelor pentru pasageri și colete
function initializeCounters() {
    const minusButtons = document.querySelectorAll('.counter-btn.minus');
    const plusButtons = document.querySelectorAll('.counter-btn.plus');
    
    minusButtons.forEach(button => {
        button.addEventListener('click', function() {
            const type = this.dataset.type;
            if (type === 'passengers' && passengersCount > 1) {
                passengersCount--;
                document.getElementById('passengers-count').textContent = passengersCount;
            } else if (type === 'luggage' && luggageCount > 0) {
                luggageCount--;
                document.getElementById('luggage-count').textContent = luggageCount;
            }
        });
    });
    
    plusButtons.forEach(button => {
        button.addEventListener('click', function() {
            const type = this.dataset.type;
            if (type === 'passengers' && passengersCount < 8) {
                passengersCount++;
                document.getElementById('passengers-count').textContent = passengersCount;
            } else if (type === 'luggage' && luggageCount < 5) {
                luggageCount++;
                document.getElementById('luggage-count').textContent = luggageCount;
            }
        });
    });
}

// Inițializarea formularului de căutare
function initializeSearchForm() {
    const searchBtn = document.querySelector('.search-btn');
    
    if (searchBtn) {
        searchBtn.addEventListener('click', function() {
            performSearch();
        });
    }
}

// Inițializarea autocomplete pentru localități
function initializeLocationAutocomplete() {
    // Această funcție este acum gestionată de locality-autocomplete.js (Google Places API)
    console.log('Location autocomplete initialized by locality-autocomplete.js');
}

// Inițializarea autocomplete pentru pagina rides
function initializeRidesPageAutocomplete() {
    // Această funcție este acum gestionată de locality-autocomplete.js (Google Places API)
    console.log('Rides page autocomplete initialized by locality-autocomplete.js');
}

// Funcțiile de autocomplete au fost mutate în locality-autocomplete.js (Google Places API)

// Inițializarea meniului hamburger
function initializeHamburgerMenu() {
    const hamburger = document.querySelector('.hamburger');
    const navMenu = document.querySelector('.nav-menu');
    const navAuth = document.querySelector('.nav-auth');
    
    if (hamburger) {
        hamburger.addEventListener('click', function(e) {
            e.stopPropagation(); // Previne propagarea click-ului
            hamburger.classList.toggle('active');
            navMenu.classList.toggle('active');
            navAuth.classList.toggle('active');
        });
    }
    
    // Închide meniul când se face click în afara lui
    document.addEventListener('click', function(e) {
        const isClickInsideNavbar = e.target.closest('.navbar');
        const isClickOnHamburger = e.target.closest('.hamburger');
        
        if (!isClickInsideNavbar && !isClickOnHamburger) {
            // Click în afara navbar-ului, închide meniul
            hamburger.classList.remove('active');
            navMenu.classList.remove('active');
            navAuth.classList.remove('active');
        }
    });
    
    // Închide meniul când se face click pe un link din meniu
    const navLinks = document.querySelectorAll('.nav-menu a, .nav-auth a, .nav-auth button');
    navLinks.forEach(link => {
        link.addEventListener('click', function() {
            hamburger.classList.remove('active');
            navMenu.classList.remove('active');
            navAuth.classList.remove('active');
        });
    });
}

// Inițializarea butonului pentru adăugarea cursei
function initializeAddRideButton() {
    const addRideBtn = document.querySelector('.add-ride-btn');
    
    if (addRideBtn) {
        addRideBtn.addEventListener('click', function(e) {
            e.preventDefault();
            checkAuthAndRedirect();
        });
    }
}

// Funcție pentru verificarea autentificării și redirecționare
function checkAuthAndRedirect() {
    fetch('/api/auth/user')
        .then(response => {
            if (response.ok) {
                // User is logged in, redirect to add-ride page
                const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
                window.location.href = '/' + currentLang + '/add-ride';
            } else {
                // User is not logged in, redirect to login page
                // Save the target URL in sessionStorage for redirection after login
                const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
                sessionStorage.setItem('redirectAfterLogin', '/' + currentLang + '/add-ride');
                window.location.href = '/' + currentLang + '/login';
            }
        })
        .catch(error => {
            console.error('Error checking auth status:', error);
            // On error, redirect to login page for safety
            const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
            sessionStorage.setItem('redirectAfterLogin', '/' + currentLang + '/add-ride');
            window.location.href = '/' + currentLang + '/login';
        });
}

// Efectuarea căutării
function performSearch() {
    const fromLocationElement = document.getElementById('from-location');
    const toLocationElement = document.getElementById('to-location');
    const travelDateElement = document.getElementById('travel-date');
    
    // Verificăm dacă elementele există (pentru a evita erorile pe pagina rides)
    if (!fromLocationElement || !toLocationElement || !travelDateElement) {
        console.log('🔍 Search elements not found, skipping performSearch');
        return;
    }
    
    const fromLocation = fromLocationElement.value;
    const toLocation = toLocationElement.value;
    const travelDate = travelDateElement.value;
    
    if (!fromLocation || !toLocation) {
        showNotification('Vă rugăm să completați localitățile de plecare și destinație.', 'warning');
        return;
    }
    
    // Construim obiectul de căutare
    const searchData = {
        fromLocation: fromLocation,
        toLocation: toLocation,
        travelDate: travelDate,
        passengers: passengersCount,
        luggage: luggageCount
    };
    
    console.log('Searching with data:', searchData);
    
    // Simulăm căutarea - în viitor va face request către backend
    showNotification('Căutarea cursei...', 'info');
    
    // Redirecționăm către pagina de curse cu parametrii de căutare
    const params = new URLSearchParams(searchData);
    window.location.href = `/rides?${params.toString()}`;
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

// Inițializarea calendarului modern cu Flatpickr pentru pagina principală
function initializeModernCalendar() {
    // Calendar pentru data călătoriei în formularul de căutare
    const travelDateInput = document.getElementById('travel-date');
    
    if (travelDateInput) {
        flatpickr(travelDateInput, {
            dateFormat: "d/m/Y",
            locale: "ro",
            minDate: "today",
            maxDate: new Date().fp_incr(365), // Până la un an în viitor
            disableMobile: false,
            allowInput: true,
            clickOpens: true,
            theme: "material_blue",
            onChange: function(selectedDates, dateStr, instance) {
                if (selectedDates.length > 0) {
                    console.log('Travel date selected:', dateStr);
                }
            },
            onReady: function(selectedDates, dateStr, instance) {
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
        
        console.log('Modern calendar initialized for main page travel date');
    }
}

// Inițializarea linkurilor pentru profilul utilizatorului
function initializeUserProfileLinks() {
    console.log('🔍 Initializing user profile links...');
    
    // Event delegation pentru linkurile de profil utilizator
    document.addEventListener('click', function(e) {
        console.log('🖱️ Click detected on:', e.target);
        
        if (e.target.closest('.user-profile-link')) {
            console.log('✅ Click on user profile link detected!');
            const link = e.target.closest('.user-profile-link');
            const userId = link.getAttribute('data-user-id');
            console.log('👤 User ID extracted:', userId);
            
            if (userId) {
                console.log('🚀 Navigating to user profile:', userId);
                navigateToUserProfile(userId);
            } else {
                console.error('❌ No user ID found in data-user-id attribute');
            }
        }
    });
    
    // Verificăm dacă există elemente cu clasa user-profile-link
    const profileLinks = document.querySelectorAll('.user-profile-link');
    console.log('🔗 Found profile links:', profileLinks.length);
    profileLinks.forEach((link, index) => {
        const userId = link.getAttribute('data-user-id');
        console.log(`🔗 Link ${index}: data-user-id="${userId}"`);
    });
}

// Navigarea la profilul utilizatorului
function navigateToUserProfile(userId) {
    console.log('🧭 navigateToUserProfile called with userId:', userId);
    
    if (userId) {
        const profileUrl = `/profile/${userId}`;
        console.log('🌐 Redirecting to:', profileUrl);
        window.location.href = profileUrl;
    } else {
        console.error('❌ User ID is missing for profile navigation.');
        showNotification('Eroare: ID-ul utilizatorului nu a fost găsit.', 'error');
    }
}

// Inițializarea formatării datelor pentru containerele de curse
function initializeRideDateFormats() {
    console.log('📅 Initializing ride date formats...');
    
    // Format date elements
    const dateElements = document.querySelectorAll('.ride-date');
    const timeElements = document.querySelectorAll('.ride-time');
    
    // Get current language
    const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
    const locale = currentLang === 'ru' ? 'ru-RU' : 'ro-RO';
    
    console.log('🌍 Current language:', currentLang, 'Locale:', locale);
    
    // Format dates
    dateElements.forEach(element => {
        const dateString = element.getAttribute('data-date');
        if (dateString) {
            const date = new Date(dateString);
            const formattedDate = date.toLocaleDateString(locale, {
                day: '2-digit',
                month: 'short',
                year: 'numeric'
            });
            element.textContent = formattedDate;
            console.log('📅 Formatted date:', dateString, '→', formattedDate);
        }
    });
    
    // Format times
    timeElements.forEach(element => {
        const timeString = element.getAttribute('data-time');
        if (timeString) {
            const time = new Date(timeString);
            const formattedTime = time.toLocaleTimeString(locale, {
                hour: '2-digit',
                minute: '2-digit'
            });
            element.textContent = formattedTime;
            console.log('🕐 Formatted time:', timeString, '→', formattedTime);
        }
    });
    
    console.log(`✅ Formatted ${dateElements.length} dates and ${timeElements.length} times`);
}

// Inițializarea butoanelor de copiere pentru numerele de telefon
function initializePhoneCopyButtons() {
    console.log('📞 Initializing phone copy buttons...');
    
    // Event delegation pentru butoanele de copiere
    document.addEventListener('click', function(e) {
        if (e.target.closest('.copy-phone-btn')) {
            e.preventDefault();
            e.stopPropagation();
            
            const button = e.target.closest('.copy-phone-btn');
            const phoneNumber = button.getAttribute('data-phone') || 
                               button.parentElement.querySelector('.phone-number')?.textContent ||
                               button.parentElement.querySelector('#phone')?.textContent;
            
            if (phoneNumber && phoneNumber !== 'Se încarcă...' && phoneNumber !== 'Nu specificat') {
                copyToClipboard(phoneNumber);
            }
        }
    });
    
    console.log('✅ Phone copy buttons initialized');
}

// Funcția de copiere în clipboard (pentru ride-details și alte pagini)
function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(function() {
        const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
        // Show success feedback
        const copyButtons = document.querySelectorAll('.copy-phone-btn');
        copyButtons.forEach(btn => {
            const btnPhoneNumber = btn.getAttribute('data-phone') || 
                                  btn.parentElement.querySelector('.phone-number')?.textContent ||
                                  btn.parentElement.querySelector('#phone')?.textContent;
            
            if (btnPhoneNumber === text) {
                btn.classList.add('copied');
                btn.innerHTML = '<i class="fas fa-check"></i>';
                
                setTimeout(() => {
                    btn.classList.remove('copied');
                    btn.innerHTML = '<i class="fas fa-copy"></i>';
                }, 2000);
            }
        });
        
        const successMessage = currentLang === 'ru'
            ? 'Номер телефона скопирован в буфер обмена!'
            : 'Numărul de telefon a fost copiat în clipboard!';
        showNotification(successMessage, 'success');
    }).catch(function(err) {
        console.error('Eroare la copierea în clipboard:', err);
        const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
        const errorMessage = currentLang === 'ru'
            ? 'Ошибка при копировании номера телефона'
            : 'Eroare la copierea numărului de telefon';
        showNotification(errorMessage, 'error');
    });
}
