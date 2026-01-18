// Profile Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('📄 Profile page loaded');
    
    // Test if add-ride-btn exists immediately
    const testBtn = document.getElementById('add-ride-btn');
    console.log('🧪 Test: add-ride-btn found:', testBtn);
    
    // Initialize profile
    initializeProfile();
    
    // Load user data
    loadUserProfile();
    
    // Setup event listeners
    setupEventListeners();
    
    // Initialize rating system
    initializeRatingSystem();
    
    // Initialize translations
    initializeTranslations();
});

// Translation object for profile page
const profileTranslations = {
    'ro': {
        'pageTitle': 'Profil',
        'loading': 'Se încarcă...',
        'stats': {
            'rides': 'Călătorii',
            'completed': 'Completate',
            'memberSince': 'Membru din',
            'rating': 'Rating'
        },
        'info': {
            'title': 'Informații',
            'name': 'Nume:',
            'email': 'Email:',
            'phone': 'Telefon:',
            'memberSince': 'Membru din:',
            'status': 'Status:',
            'active': 'Activ',
            'notSpecified': 'Nu specificat'
        },
        'actions': {
            'title': 'Acțiuni',
            'editProfile': 'Editează profilul',
            'addRide': 'Adaugă cursă',
            'logout': 'Deconectare',
            'contact': 'Contactează',
            'viewRides': 'Vezi cursele',
            'report': 'Raportează'
        },
        'achievements': {
            'title': 'Realizări',
            'firstRide': 'Prima cursă',
            'firstRideDesc': 'Ai creat prima ta cursă',
            'fiveRides': '5 curse',
            'fiveRidesDesc': 'Ai creat 5 curse',
            'tenRides': '10 curse',
            'tenRidesDesc': 'Ai creat 10 curse',
            'firstRating': 'Primul rating',
            'firstRatingDesc': 'Ai primit primul rating',
            'fiveRatings': '5 rating-uri',
            'fiveRatingsDesc': 'Ai primit 5 rating-uri',
            'perfectRating': 'Rating perfect',
            'perfectRatingDesc': 'Ai un rating de 5 stele'
        },
        'rides': {
            'title': 'Călătoriile mele',
            'otherTitle': 'Călătoriile utilizatorului',
            'active': 'Active',
            'completed': 'Completate',
            'noRides': 'Nu ai încă călătorii',
            'noRidesOther': 'Nu are încă călătorii',
            'noRidesDesc': 'Începe să creezi călătorii pentru a le vedea aici',
            'noRidesDescOther': 'Acest utilizator nu a creat încă nicio călătorie',
            'addFirstRide': 'Adaugă prima cursă',
            'viewRide': 'Vezi cursă',
            'editRide': 'Editează',
            'deleteRide': 'Șterge',
            'deleteConfirm': 'Ești sigur că vrei să ștergi această cursă?',
            'deleteSuccess': 'Cursa a fost ștearsă cu succes!',
            'deleteError': 'Eroare la ștergerea cursei',
            'package_only': 'Transport doar colete',
            'available_seats_text': 'locuri',
            'transport_and_packages': 'Transport și colete',
            'views': 'Vizualizări'
        },
        'rating': {
            'title': 'Rating & Comentarii',
            'yourRatings': 'Rating-urile tale',
            'receivedRatings': 'Rating-uri primite',
            'giveRating': 'Dă un rating',
            'yourRating': 'Rating-ul tău',
            'loginRequired': 'Cont necesar pentru rating',
            'loginRequiredDesc': 'Trebuie să ai un cont pentru a putea da rating și comentarii la acest profil.',
            'login': 'Conectează-te',
            'comment': 'Comentariu (opțional):',
            'commentPlaceholder': 'Spune-ne ce părere ai despre această persoană...',
                    'submitRating': 'Trimite rating',
        'selectRating': 'Te rog să selectezi un rating!',
        'ratings': 'rating-uri',
        'noRatings': 'Nu există încă rating-uri pentru acest utilizator.'
        },
        'roles': {
            'admin': 'ADMIN',
            'moderator': 'MODERATOR'
        }
    },
    'ru': {
        'pageTitle': 'Профиль',
        'loading': 'Загрузка...',
        'stats': {
            'rides': 'Поездки',
            'completed': 'Завершённые',
            'memberSince': 'Участник с',
            'rating': 'Рейтинг'
        },
        'info': {
            'title': 'Информация',
            'name': 'Имя:',
            'email': 'Email:',
            'phone': 'Телефон:',
            'memberSince': 'Участник с:',
            'status': 'Статус:',
            'active': 'Активный',
            'notSpecified': 'Не указано'
        },
        'actions': {
            'title': 'Действия',
            'editProfile': 'Редактировать профиль',
            'addRide': 'Добавить поездку',
            'logout': 'Выйти',
            'contact': 'Связаться',
            'viewRides': 'Посмотреть поездки',
            'report': 'Пожаловаться'
        },
        'achievements': {
            'title': 'Достижения',
            'firstRide': 'Первая поездка',
            'firstRideDesc': 'Вы создали свою первую поездку',
            'fiveRides': '5 поездок',
            'fiveRidesDesc': 'Вы создали 5 поездок',
            'tenRides': '10 поездок',
            'tenRidesDesc': 'Вы создали 10 поездок',
            'firstRating': 'Первый рейтинг',
            'firstRatingDesc': 'Вы получили первый рейтинг',
            'fiveRatings': '5 рейтингов',
            'fiveRatingsDesc': 'Вы получили 5 рейтингов',
            'perfectRating': 'Идеальный рейтинг',
            'perfectRatingDesc': 'У вас рейтинг 5 звёзд'
        },
        'rides': {
            'title': 'Мои поездки',
            'otherTitle': 'Поездки пользователя',
            'active': 'Активные',
            'completed': 'Завершённые',
            'noRides': 'У вас пока нет поездок',
            'noRidesOther': 'У него пока нет поездок',
            'noRidesDesc': 'Начните создавать поездки, чтобы увидеть их здесь',
            'noRidesDescOther': 'Этот пользователь пока не создал ни одной поездки',
            'addFirstRide': 'Добавить первую поездку',
            'viewRide': 'Посмотреть поездку',
            'editRide': 'Редактировать',
            'deleteRide': 'Удалить',
            'deleteConfirm': 'Вы уверены, что хотите удалить эту поездку?',
            'deleteSuccess': 'Поездка успешно удалена!',
            'deleteError': 'Ошибка при удалении поездки',
            'package_only': 'Транспортирую только посылки',
            'available_seats_text': 'мест',
            'transport_and_packages': 'Транспорт и посылки',
            'views': 'Просмотры'
        },
        'rating': {
            'title': 'Рейтинг и комментарии',
            'yourRatings': 'Ваши рейтинги',
            'receivedRatings': 'Полученные рейтинги',
            'giveRating': 'Поставить рейтинг',
            'yourRating': 'Ваш рейтинг',
            'loginRequired': 'Требуется аккаунт для рейтинга',
            'loginRequiredDesc': 'Вам нужен аккаунт, чтобы ставить рейтинги и комментарии к этому профилю.',
            'login': 'Войти',
            'comment': 'Комментарий (необязательно):',
            'commentPlaceholder': 'Расскажите, что вы думаете об этом человеке...',
                    'submitRating': 'Отправить рейтинг',
        'selectRating': 'Пожалуйста, выберите рейтинг!',
        'ratings': 'рейтингов',
        'noRatings': 'Пока нет рейтингов для этого пользователя.'
        },
        'roles': {
            'admin': 'АДМИН',
            'moderator': 'МОДЕРАТОР'
        }
    }
};

function getCurrentLanguage() {
    const currentLangElement = document.querySelector('.current-lang');
    return currentLangElement ? (currentLangElement.textContent === 'RO' ? 'ro' : 'ru') : 'ro';
}

function translateText(key) {
    const lang = getCurrentLanguage();
    const translation = profileTranslations[lang];
    
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

function initializeTranslations() {
    // Update page title
    document.title = translateText('pageTitle') + ' - Rutex';
    
    // Update stats labels
    const statsLabels = document.querySelectorAll('.stat-label');
    if (statsLabels.length >= 4) {
        statsLabels[0].textContent = translateText('stats.rides');
        statsLabels[1].textContent = translateText('stats.completed');
        statsLabels[2].textContent = translateText('stats.memberSince');
        statsLabels[3].textContent = translateText('stats.rating');
    }
    
    // Update section titles
    const infoTitle = document.querySelector('.info-section h3');
    if (infoTitle) infoTitle.innerHTML = '<i class="fas fa-user"></i> ' + translateText('info.title');
    
    const actionsTitle = document.querySelector('.actions-section h3');
    if (actionsTitle) actionsTitle.innerHTML = '<i class="fas fa-cog"></i> ' + translateText('actions.title');
    
    const achievementsTitle = document.querySelector('.achievements-section h3');
    if (achievementsTitle) achievementsTitle.innerHTML = '<i class="fas fa-trophy"></i> ' + translateText('achievements.title');
    
    const ridesTitle = document.querySelector('#rides-section-title');
    if (ridesTitle) {
        // Verificăm dacă suntem pe profilul propriu sau al altui utilizator
        const pathSegments = window.location.pathname.split('/');
        const targetUserId = pathSegments.length > 3 && pathSegments[2] === 'profile' ? pathSegments[3] : 
                           pathSegments.length > 2 && pathSegments[1] === 'profile' ? pathSegments[2] : null;
        const isOwnProfile = !targetUserId || targetUserId === 'edit-profile';
        ridesTitle.textContent = isOwnProfile ? translateText('rides.title') : translateText('rides.otherTitle');
    }
    
    const ratingTitle = document.querySelector('#rating-section-title');
    if (ratingTitle) ratingTitle.textContent = translateText('rating.title');
    
    // Update info labels
    const infoLabels = document.querySelectorAll('.info-item label');
    if (infoLabels.length >= 5) {
        infoLabels[0].textContent = translateText('info.name');
        infoLabels[1].textContent = translateText('info.email');
        infoLabels[2].textContent = translateText('info.phone');
        infoLabels[3].textContent = translateText('info.memberSince');
        infoLabels[4].textContent = translateText('info.status');
    }
    
    // Update status text
    const statusSpan = document.querySelector('#user-status');
    if (statusSpan) statusSpan.textContent = translateText('info.active');
    
    // Update action buttons
    const editProfileBtn = document.getElementById('edit-profile-btn');
    if (editProfileBtn) editProfileBtn.innerHTML = '<i class="fas fa-edit"></i> ' + translateText('actions.editProfile');
    
    const addRideBtn = document.getElementById('add-ride-btn');
    if (addRideBtn) addRideBtn.innerHTML = '<i class="fas fa-plus"></i> ' + translateText('actions.addRide');
    
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) logoutBtn.innerHTML = '<i class="fas fa-sign-out-alt"></i> ' + translateText('actions.logout');
    
    const contactBtn = document.getElementById('contact-user-btn');
    if (contactBtn) contactBtn.innerHTML = '<i class="fas fa-envelope"></i> ' + translateText('actions.contact');
    
    const viewRidesBtn = document.getElementById('view-all-rides-btn');
    if (viewRidesBtn) viewRidesBtn.innerHTML = '<i class="fas fa-car"></i> ' + translateText('actions.viewRides');
    
    const reportBtn = document.getElementById('report-user-btn');
    if (reportBtn) reportBtn.innerHTML = '<i class="fas fa-flag"></i> ' + translateText('actions.report');
    
    // Update tab buttons
    const tabButtons = document.querySelectorAll('.tab-btn');
    if (tabButtons.length >= 2) {
        tabButtons[0].textContent = translateText('rides.active');
        tabButtons[1].textContent = translateText('rides.completed');
    }
    
    // Update no rides section - these will be updated dynamically in displayUserRides
    // based on whether it's own profile or other user's profile
    
    const addFirstRideBtn = document.getElementById('add-first-ride-btn');
    if (addFirstRideBtn) addFirstRideBtn.innerHTML = '<i class="fas fa-plus"></i> ' + translateText('rides.addFirstRide');
    
    // Update loading text
    const loadingElements = document.querySelectorAll('.loading-spinner p');
    loadingElements.forEach(element => {
        if (element.textContent === 'Se încarcă...') {
            element.textContent = translateText('loading');
        }
    });
    
    // Update role indicators
    const roleIndicators = document.querySelectorAll('.role-indicator');
    roleIndicators.forEach(indicator => {
        if (indicator.textContent === 'ADMIN') {
            indicator.textContent = translateText('roles.admin');
        } else if (indicator.textContent === 'MODERATOR') {
            indicator.textContent = translateText('roles.moderator');
        }
    });
    
    // Update rating section elements
    const ratingFormTitle = document.querySelector('.rating-form h4');
    if (ratingFormTitle) ratingFormTitle.textContent = translateText('rating.giveRating');
    
    const ratingUpdateTitle = document.querySelector('.rating-update h4');
    if (ratingUpdateTitle) ratingUpdateTitle.textContent = translateText('rating.yourRating');
    
    const yourRatingsTitle = document.querySelector('.user-ratings h4');
    if (yourRatingsTitle) {
        const pathSegments = window.location.pathname.split('/');
        const targetUserId = pathSegments.length > 2 && pathSegments[1] === 'profile' ? pathSegments[2] : null;
        const isOwnProfile = !targetUserId || targetUserId === 'edit-profile';
        yourRatingsTitle.textContent = isOwnProfile ? translateText('rating.yourRatings') : translateText('rating.receivedRatings');
    }
    
    const loginRequiredTitle = document.querySelector('.login-required-message h4');
    if (loginRequiredTitle) loginRequiredTitle.textContent = translateText('rating.loginRequired');
    
    const loginRequiredDesc = document.querySelector('.login-required-message p');
    if (loginRequiredDesc) loginRequiredDesc.textContent = translateText('rating.loginRequiredDesc');
    
    const loginBtn = document.getElementById('login-redirect-btn');
    if (loginBtn) loginBtn.textContent = translateText('rating.login');
    
    const commentLabel = document.querySelector('label[for="rating-comment"]');
    if (commentLabel) commentLabel.textContent = translateText('rating.comment');
    
    const commentField = document.getElementById('rating-comment');
    if (commentField) commentField.placeholder = translateText('rating.commentPlaceholder');
    
    const submitRatingBtn = document.getElementById('submit-rating-btn');
    if (submitRatingBtn) submitRatingBtn.innerHTML = '<i class="fas fa-paper-plane"></i> ' + translateText('rating.submitRating');
}

// Funcție pentru verificarea autentificării
function checkAuthentication() {
    // Verificăm dacă suntem pe o rută de profil specific
    const pathSegments = window.location.pathname.split('/');
    
    // Handle both language-prefixed URLs (/ro/profile/123) and non-prefixed URLs (/profile/123)
    let targetUserId = null;
    if (pathSegments.length > 3 && pathSegments[2] === 'profile') {
        // Language-prefixed URL: /ro/profile/123 or /ru/profile/123
        targetUserId = pathSegments[3];
    } else if (pathSegments.length > 2 && pathSegments[1] === 'profile') {
        // Non-prefixed URL: /profile/123
        targetUserId = pathSegments[2];
    }
    
    // Dacă suntem pe profilul propriu (fără userId în URL), verificăm autentificarea
    if (!targetUserId) {
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
                    // Utilizatorul nu este autentificat, redirecționăm la logare
                    // Salvăm URL-ul curent pentru a reveni după logare
                    sessionStorage.setItem('redirectAfterLogin', window.location.pathname);
                    const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
                    window.location.href = '/' + currentLang + '/login';
                }
            })
            .catch(error => {
                console.error('Error checking auth status:', error);
                // În caz de eroare, redirecționăm la logare
                sessionStorage.setItem('redirectAfterLogin', window.location.pathname);
                const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
                window.location.href = '/' + currentLang + '/login';
            });
    }
    // Dacă suntem pe profilul cuiva (targetUserId există), nu verificăm autentificarea
    // Permitem accesul public la profilurile altor utilizatori
}

function initializeProfile() {
    // Add smooth scrolling
    addSmoothScrolling();
    
    // Add avatar hover effect
    const avatar = document.querySelector('.profile-avatar');
    if (avatar) {
        avatar.addEventListener('mouseenter', function() {
            this.style.transform = 'scale(1.05)';
        });
        
        avatar.addEventListener('mouseleave', function() {
            this.style.transform = 'scale(1)';
        });
    }
}

function addSmoothScrolling() {
    // Add smooth scrolling to all internal links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

function setupEventListeners() {
    console.log('🔧 Setting up event listeners...');
    
    // Test: adăugăm un event listener pe body pentru a verifica dacă JavaScript-ul funcționează
    document.body.addEventListener('click', function(e) {
        if (e.target.id === 'add-ride-btn') {
            console.log('🎯 Add ride button clicked via body listener!');
        }
        if (e.target.id === 'add-first-ride-btn') {
            console.log('🎯 Add first ride button clicked via body listener!');
            const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
            const targetUrl = '/' + currentLang + '/add-ride';
            console.log('🎯 Redirecting to:', targetUrl);
            window.location.href = targetUrl;
        }
    });
    
    // Edit profile button
    const editProfileBtn = document.getElementById('edit-profile-btn');
    if (editProfileBtn) {
        editProfileBtn.addEventListener('click', () => {
            const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
            window.location.href = '/' + currentLang + '/edit-profile';
        });
    }

    // Add ride button
    const addRideBtn = document.getElementById('add-ride-btn');
    console.log('🔍 Looking for add-ride-btn:', addRideBtn);
    console.log('🔍 All buttons on page:', document.querySelectorAll('button'));
    console.log('🔍 All elements with id containing "add":', document.querySelectorAll('[id*="add"]'));
    if (addRideBtn) {
        addRideBtn.addEventListener('click', () => {
            const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
            console.log('🚗 Add ride button clicked');
            console.log('🌍 Current language detected:', currentLang);
            const targetUrl = '/' + currentLang + '/add-ride';
            console.log('🎯 Redirecting to:', targetUrl);
            window.location.href = targetUrl;
        });
    } else {
        console.error('❌ Add ride button not found!');
    }

    // Logout button
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            fetch('/api/auth/logout', { method: 'POST' })
                .then(() => {
                    window.location.href = '/';
                })
                .catch(error => {
                    console.error('Logout error:', error);
                    window.location.href = '/';
                });
        });
    }

    // Rating form submission
    const ratingFormElement = document.getElementById('rating-form-element');
    if (ratingFormElement) {
        ratingFormElement.addEventListener('submit', function(e) {
            e.preventDefault();
            submitRating();
        });
    }





    // Contact user button
    const contactUserBtn = document.getElementById('contact-user-btn');
    if (contactUserBtn) {
        contactUserBtn.addEventListener('click', () => {
            showNotification('Funcționalitatea de contact va fi implementată în curând!', 'info');
        });
    }

    // View all rides button
    const viewAllRidesBtn = document.getElementById('view-all-rides-btn');
    if (viewAllRidesBtn) {
        viewAllRidesBtn.addEventListener('click', () => {
            // Scroll to rides section
            document.getElementById('user-rides-container').scrollIntoView({ behavior: 'smooth' });
        });
    }

    // Report user button
    const reportUserBtn = document.getElementById('report-user-btn');
    if (reportUserBtn) {
        reportUserBtn.addEventListener('click', () => {
            showNotification('Funcționalitatea de raportare va fi implementată în curând!', 'info');
        });
    }

    // Add first ride button (from no rides section)
    const addFirstRideBtn = document.getElementById('add-first-ride-btn');
    if (addFirstRideBtn) {
        addFirstRideBtn.addEventListener('click', () => {
            const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
            console.log('🚗 Add first ride button clicked');
            console.log('🌍 Current language detected:', currentLang);
            const targetUrl = '/' + currentLang + '/add-ride';
            console.log('🎯 Redirecting to:', targetUrl);
            window.location.href = targetUrl;
        });
    } else {
        console.log('ℹ️ Add first ride button not found (probably not displayed yet)');
    }
}

function setupOwnProfileMode() {
    console.log('👤 Setting up own profile mode');
    
    // Setup tab switching for own profile
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            switchTab(this.dataset.tab);
        });
    });
    
    console.log('✅ Own profile mode configured');
}

function setupViewProfileMode() {
    console.log('👥 Setting up view profile mode');
    
    // Setup tab switching for view profile
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            switchTab(this.dataset.tab);
        });
    });
    
    console.log('✅ View profile mode configured');
}

function loadUserProfile() {
    console.log('🔍 loadUserProfile called');
    // Show loading state
    showLoadingState();
    
    // Verificăm dacă suntem pe o rută de profil specific
    const pathSegments = window.location.pathname.split('/');
    
    // Handle both language-prefixed URLs (/ro/profile/123) and non-prefixed URLs (/profile/123)
    let targetUserId = null;
    if (pathSegments.length > 3 && pathSegments[2] === 'profile') {
        // Language-prefixed URL: /ro/profile/123 or /ru/profile/123
        targetUserId = pathSegments[3];
    } else if (pathSegments.length > 2 && pathSegments[1] === 'profile') {
        // Non-prefixed URL: /profile/123
        targetUserId = pathSegments[2];
    }
    
    console.log('📍 Current path:', window.location.pathname);
    console.log('🔢 Path segments:', pathSegments);
    console.log('👤 Target User ID:', targetUserId);
    
    // Set initial display states based on profile type
    const quickActionsSection = document.getElementById('quick-actions-section');
    const viewProfileActionsSection = document.getElementById('view-profile-actions-section');
    const ratingSection = document.getElementById('rating-section');
    
    if (targetUserId && targetUserId !== 'edit-profile') {
        // Viewing another user's profile
        if (quickActionsSection) quickActionsSection.style.display = 'none';
        if (viewProfileActionsSection) viewProfileActionsSection.style.display = 'block';
        if (ratingSection) ratingSection.style.display = 'block';
        console.log('✅ Loading specific user profile for ID:', targetUserId);
        // Încărcăm profilul unui utilizator specific
        loadSpecificUserProfile(targetUserId);
    } else {
        // Viewing own profile
        if (quickActionsSection) quickActionsSection.style.display = 'block';
        if (viewProfileActionsSection) viewProfileActionsSection.style.display = 'none';
        if (ratingSection) ratingSection.style.display = 'block';
        console.log('👤 Loading current user profile');
        // Încărcăm profilul utilizatorului logat
        loadCurrentUserProfile();
    }
}

function loadSpecificUserProfile(userId) {
    console.log('🔍 loadSpecificUserProfile called with userId:', userId);
    
    fetch(`/api/users/${userId}`)
        .then(response => {
            console.log('📡 API response status:', response.status);
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('User not found');
            }
        })
        .then(user => {
            console.log('👤 User data received:', user);
            hideLoadingState();
            displayUserInfo(user, false); // false = nu este profilul propriu
            loadSpecificUserRides(userId);
            setupViewProfileMode(); // Activăm modul de vizualizare
            loadUserRatingData(userId); // Load rating data
        })
        .catch(error => {
            console.error('❌ Error loading specific user profile:', error);
            hideLoadingState();
            showNotification('Utilizatorul nu a fost găsit!', 'error');
            // Redirecționăm la pagina principală în loc de profilul propriu
            const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
            window.location.href = '/' + currentLang;
        });
}

function loadCurrentUserProfile() {
    // Check authentication first for own profile
    checkAuthentication();
    
    fetch('/api/auth/user')
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('User not authenticated');
            }
        })
        .then(user => {
            hideLoadingState();
            displayUserInfo(user, true); // true = este profilul propriu
            loadUserRides();
            setupOwnProfileMode(); // Activăm modul propriu
            loadUserRatingData(user.id); // Load rating data for own profile
        })
        .catch(error => {
            console.error('Error loading user profile:', error);
            hideLoadingState();
            // Don't show error notification for non-authenticated users
            // Just hide loading state and let the page display properly
        });
}

function loadSpecificUserRides(userId) {
    showRidesLoading();
    
    // Încărcăm cursele utilizatorului specific
    fetch(`/api/rides/user/${userId}`)
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Failed to load user rides');
            }
        })
        .then(data => {
            hideRidesLoading();
            displayUserRides(data, false); // false = nu sunt cursele proprii
            updateRideStats(data);
            updateAchievements(data);
        })
        .catch(error => {
            console.error('Error loading specific user rides:', error);
            hideRidesLoading();
            showNotification('Eroare la încărcarea călătoriilor utilizatorului!', 'error');
        });
}

function loadUserRides() {
    showRidesLoading();
    
    fetch('/api/rides/my-rides')
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Failed to load rides');
            }
        })
        .then(data => {
            hideRidesLoading();
            displayUserRides(data, true); // true = sunt cursele proprii
            updateRideStats(data);
            updateAchievements(data);
        })
        .catch(error => {
            console.error('Error loading user rides:', error);
            hideRidesLoading();
            showNotification('Eroare la încărcarea călătoriilor!', 'error');
        });
}

function showLoadingState() {
    const profileElements = document.querySelectorAll('.profile-name, .profile-email, .stat-number, .info-item span');
    profileElements.forEach(element => {
        element.style.opacity = '0.5';
    });
}

function hideLoadingState() {
    const profileElements = document.querySelectorAll('.profile-name, .profile-email, .stat-number, .info-item span');
    profileElements.forEach(element => {
        element.style.opacity = '1';
    });
}

function showRidesLoading() {
    const ridesLoading = document.getElementById('rides-loading');
    const ridesList = document.getElementById('user-rides-list');
    const noRides = document.getElementById('no-rides');
    
    if (ridesLoading) ridesLoading.style.display = 'flex';
    if (ridesList) ridesList.style.display = 'none';
    if (noRides) noRides.style.display = 'none';
}

function hideRidesLoading() {
    const ridesLoading = document.getElementById('rides-loading');
    if (ridesLoading) ridesLoading.style.display = 'none';
}

function showNoRides() {
    const ridesList = document.getElementById('user-rides-list');
    const noRides = document.getElementById('no-rides');
    
    console.log('📋 showNoRides called');
    console.log('📋 ridesList found:', ridesList);
    console.log('📋 noRides found:', noRides);
    
    if (ridesList) ridesList.style.display = 'none';
    if (noRides) noRides.style.display = 'block';
    
    // Verificăm dacă butonul add-first-ride-btn există în noRides
    const addFirstRideBtn = document.getElementById('add-first-ride-btn');
    console.log('📋 add-first-ride-btn found:', addFirstRideBtn);
}

function displayUserInfo(user, isOwnProfile) {
    console.log('🔍 displayUserInfo called with user:', user, 'isOwnProfile:', isOwnProfile);
    
    try {
        // Update profile image
        const profileImage = document.getElementById('profile-image');
        const defaultAvatar = document.getElementById('default-avatar');
        
        console.log('🖼️ Profile image elements:', { profileImage, defaultAvatar });
        
        if (user.profileImage) {
            // Check if it's a Cloudinary URL (starts with http/https) or local file
            if (user.profileImage.startsWith('http://') || user.profileImage.startsWith('https://')) {
                // It's a Cloudinary URL, use it directly
                profileImage.src = user.profileImage;
            } else {
                // It's a local file, use the old path
                profileImage.src = `/uploads/profile-images/${user.profileImage}`;
            }
            profileImage.style.display = 'block';
            defaultAvatar.style.display = 'none';
        } else {
            profileImage.style.display = 'none';
            defaultAvatar.style.display = 'block';
        }
        
        console.log('✅ Profile image updated');
        
        // Update user information with smooth transitions
        updateProfileNameWithRole(user);
        animateTextChange('user-email', user.email);
        animateTextChange('full-name', `${user.firstName} ${user.lastName}`);
        animateTextChange('email', user.email);
        // Set phone with copy functionality
        setPhoneWithCopy(user.phone || 'Nu specificat');
        animateTextChange('created-at', formatDate(user.createdAt));
        
        console.log('✅ User info updated');
        
        // Update user status
        const userStatus = document.getElementById('user-status');
        if (userStatus) {
            userStatus.textContent = 'Activ';
            userStatus.style.color = '#10b981';
        }
        
        // Update user roles - only show roles section if user has admin or moderator roles
        const userRoles = document.getElementById('user-roles');
        const rolesInfoItem = document.getElementById('roles-info-item');
        
        if (userRoles && user.roles) {
            userRoles.innerHTML = '';
            let hasSpecialRoles = false;
            
            user.roles.forEach(role => {
                // Nu afișăm ROLE_USER, doar ADMIN și MOD
                if (role.name === 'ROLE_ADMIN') {
                    const roleBadge = document.createElement('span');
                    roleBadge.className = 'role-badge admin-badge';
                    roleBadge.textContent = '👑 ADMIN';
                    userRoles.appendChild(roleBadge);
                    hasSpecialRoles = true;
                } else if (role.name === 'ROLE_MOD') {
                    const roleBadge = document.createElement('span');
                    roleBadge.className = 'role-badge mod-badge';
                    roleBadge.textContent = '🛡️ MOD';
                    userRoles.appendChild(roleBadge);
                    hasSpecialRoles = true;
                }
                // ROLE_USER nu se afișează
            });
            
            // Show/hide the entire roles section based on whether user has special roles
            if (rolesInfoItem) {
                rolesInfoItem.style.display = hasSpecialRoles ? 'flex' : 'none';
            }
        }
        
        // Update member since
        animateTextChange('member-since-header', formatDate(user.createdAt));
        
        // Update rating from user data - only for display purposes
        // The actual rating data will be loaded separately by loadUserRatingData
        if (user.averageRating !== null && user.averageRating !== undefined && user.averageRating > 0) {
            animateTextChange('rating-header', user.averageRating.toFixed(1));
        } else {
            animateTextChange('rating-header', '0.0');
        }
        
        console.log('✅ Rating updated');
        
        // Show appropriate sections based on profile type
        if (isOwnProfile) {
            console.log('👤 Setting up own profile mode');
            const ridesSectionTitle = document.getElementById('rides-section-title');
            
            if (ridesSectionTitle) ridesSectionTitle.textContent = translateText('rides.title');
        } else {
            console.log('👥 Setting up view profile mode');
            const ridesSectionTitle = document.getElementById('rides-section-title');
            
            if (ridesSectionTitle) ridesSectionTitle.textContent = translateText('rides.otherTitle');
        }
        
        console.log('✅ Profile sections configured');
        
    } catch (error) {
        console.error('❌ Error in displayUserInfo:', error);
        showNotification('Eroare la afișarea informațiilor utilizatorului!', 'error');
    }
}

function updateProfileNameWithRole(user) {
    console.log('🔤 updateProfileNameWithRole called for user:', user);
    
    const userNameElement = document.getElementById('user-name');
    if (!userNameElement) {
        console.error('❌ user-name element not found');
        return;
    }
    
    // Check if Thymeleaf has already rendered the name with styling
    const existingNameSpan = userNameElement.querySelector('span[class*="user-name"]');
    
    if (existingNameSpan) {
        // Thymeleaf has already rendered the name with proper styling
        // Just update the text content without changing classes
        existingNameSpan.textContent = `${user.firstName} ${user.lastName}`;
        console.log('✅ Updated existing Thymeleaf-rendered name');
        return;
    }
    
    // If no existing styled span, then create one (fallback)
    // Clear existing content
    userNameElement.innerHTML = '';
    
    // Add the user's name with role-based styling
    const nameSpan = document.createElement('span');
    nameSpan.textContent = `${user.firstName} ${user.lastName}`;
    
    // Apply role-based CSS classes to the name span
    if (user.roles) {
        const isAdmin = user.roles.some(role => role.name === 'ROLE_ADMIN');
        const isMod = user.roles.some(role => role.name === 'ROLE_MOD');
        
        if (isAdmin) {
            nameSpan.className = 'user-name admin';
        } else if (isMod) {
            nameSpan.className = 'user-name moderator';
        } else {
            nameSpan.className = 'user-name';
        }
    } else {
        nameSpan.className = 'user-name';
    }
    
    userNameElement.appendChild(nameSpan);
    
    // Add role indicators if user has special roles
    if (user.roles) {
        user.roles.forEach(role => {
            if (role.name === 'ROLE_ADMIN') {
                const roleIndicator = document.createElement('span');
                roleIndicator.className = 'role-indicator admin';
                roleIndicator.textContent = 'ADMIN';
                userNameElement.appendChild(roleIndicator);
            } else if (role.name === 'ROLE_MOD') {
                const roleIndicator = document.createElement('span');
                roleIndicator.className = 'role-indicator moderator';
                roleIndicator.textContent = 'MODERATOR';
                userNameElement.appendChild(roleIndicator);
            }
        });
    }
    
    // Apply animation
    userNameElement.style.opacity = '0';
    setTimeout(() => {
        userNameElement.style.opacity = '1';
        console.log('✅ Profile name with role indicators updated');
    }, 150);
}

function animateTextChange(elementId, newText) {
    console.log('🔤 animateTextChange called for elementId:', elementId, 'with text:', newText);
    
    const element = document.getElementById(elementId);
    if (!element) {
        console.error('❌ Element not found:', elementId);
        return;
    }
    
    console.log('✅ Element found:', element);
    
    element.style.opacity = '0';
    setTimeout(() => {
        element.textContent = newText;
        element.style.opacity = '1';
        console.log('✅ Text updated for:', elementId);
    }, 150);
}

function displayUserRides(rides, isOwnRides) {
    const ridesList = document.getElementById('user-rides-list');
    const noRides = document.getElementById('no-rides');
    const noRidesTitle = document.getElementById('no-rides-title');
    const noRidesDescription = document.getElementById('no-rides-description');
    const addFirstRideBtn = document.getElementById('add-first-ride-btn');
    
    if (!rides || rides.length === 0) {
        showNoRides();
        if (noRidesTitle) {
            noRidesTitle.textContent = isOwnRides ? translateText('rides.noRides') : translateText('rides.noRidesOther');
        }
        if (noRidesDescription) {
            noRidesDescription.textContent = isOwnRides ? translateText('rides.noRidesDesc') : translateText('rides.noRidesDescOther');
        }
        if (addFirstRideBtn) {
            addFirstRideBtn.style.display = isOwnRides ? 'inline-flex' : 'none';
            console.log('🔘 Add first ride button display set to:', isOwnRides ? 'inline-flex' : 'none');
        } else {
            console.error('❌ Add first ride button not found in displayUserRides');
        }
        return;
    }
    
    ridesList.innerHTML = '';
    ridesList.style.display = 'flex';
    noRides.style.display = 'none';
    
    rides.forEach((ride, index) => {
        const rideElement = createRideElement(ride, isOwnRides);
        rideElement.style.opacity = '0';
        rideElement.style.transform = 'translateY(10px)';
        ridesList.appendChild(rideElement);
        
        // Animate in
        setTimeout(() => {
            rideElement.style.transition = 'all 0.3s ease';
            rideElement.style.opacity = '1';
            rideElement.style.transform = 'translateY(0)';
        }, index * 100);
    });
}

function createRideElement(ride, isOwnRides) {
    const rideElement = document.createElement('div');
    rideElement.className = 'user-ride-item';
    
    const statusClass = ride.isActive ? 'active' : 'completed';
    const statusText = ride.isActive ? translateText('rides.active') : translateText('rides.completed');
    
    // Format date and time based on current language
    const travelDate = new Date(ride.travelDate);
    const currentLang = getCurrentLanguage();
    const locale = currentLang === 'ru' ? 'ru-RU' : 'ro-RO';
    
    const formattedDate = travelDate.toLocaleDateString(locale, {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
    
    const departureTime = new Date(ride.departureTime);
    const formattedTime = departureTime.toLocaleTimeString(locale, {
        hour: '2-digit',
        minute: '2-digit'
    });
    
    // Show full locality names for both languages
    const fromLocationDisplay = ride.fromLocation;
    const toLocationDisplay = ride.toLocation;
    
    rideElement.innerHTML = `
        <div class="user-ride-header">
            <div class="user-ride-route">
                <div class="route-point">
                    <i class="fas fa-map-marker-alt"></i>
                    <span>${fromLocationDisplay}</span>
                </div>
                <div class="route-arrow">
                    <i class="fas fa-arrow-right"></i>
                </div>
                <div class="route-point">
                    <i class="fas fa-map-marker-alt"></i>
                    <span>${toLocationDisplay}</span>
                </div>
            </div>
            <div class="user-ride-status ${statusClass}">${statusText}</div>
        </div>
        <div class="user-ride-details">
            <div class="user-ride-detail">
                <i class="fas fa-calendar"></i>
                <span>${formattedDate}</span>
            </div>
            <div class="user-ride-detail">
                <i class="fas fa-clock"></i>
                <span>${formattedTime}</span>
            </div>
            ${ride.isPackageOnly ? `
                <div class="user-ride-detail">
                    <i class="fas fa-box"></i>
                    <span style="color: #fb7185; font-weight: 600;">${translateText('rides.package_only')}</span>
                </div>
            ` : `
                <div class="user-ride-detail">
                    <i class="fas fa-users"></i>
                    <span>${ride.availableSeats} ${translateText('rides.available_seats_text')}</span>
                </div>
            `}
            ${!ride.isPackageOnly && ride.transportAndPackages ? `
                <div class="user-ride-detail">
                    <i class="fas fa-box"></i>
                    <span style="color: #3b82f6; font-weight: 600;">${translateText('rides.transport_and_packages')}</span>
                </div>
            ` : ''}
            <div class="user-ride-detail">
                <i class="fas fa-money-bill"></i>
                                        <span class="user-ride-price">${ride.price} MDL</span>
            </div>
        </div>
        ${ride.description ? `<div class="ride-description">${ride.description}</div>` : ''}
        
        <!-- View count indicator -->
        <div class="view-count-indicator">
            <i class="fas fa-eye"></i>
            <span>${translateText('rides.views')}: <span>${ride.viewCount || 0}</span></span>
        </div>
        
        <div class="user-ride-actions">
            <button class="btn btn-primary btn-small" onclick="viewRide(${ride.id})">
                <i class="fas fa-eye"></i>
                ${translateText('rides.viewRide')}
            </button>
            ${ride.isActive && isOwnRides ? `
                <button class="btn btn-secondary btn-small" onclick="editRide(${ride.id})">
                    <i class="fas fa-edit"></i>
                    ${translateText('rides.editRide')}
                </button>
                <button class="btn btn-danger btn-small" onclick="deleteRide(${ride.id})">
                    <i class="fas fa-trash"></i>
                    ${translateText('rides.deleteRide')}
                </button>
            ` : ''}
        </div>
    `;
    
    return rideElement;
}

function updateRideStats(rides) {
    const totalRides = rides.length;
    const completedRides = rides.filter(ride => !ride.isActive).length;
    
    animateStatChange('total-rides-header', totalRides);
    animateStatChange('completed-rides-header', completedRides);
}

function animateStatChange(elementId, newValue) {
    const element = document.getElementById(elementId);
    if (!element) return;
    
    const currentValue = parseInt(element.textContent) || 0;
    const increment = (newValue - currentValue) / 20;
    let current = currentValue;
    
    const timer = setInterval(() => {
        current += increment;
        if ((increment > 0 && current >= newValue) || (increment < 0 && current <= newValue)) {
            element.textContent = newValue;
            clearInterval(timer);
        } else {
            element.textContent = Math.floor(current);
        }
    }, 50);
}

function updateAchievements(rides = []) {
    const achievementsList = document.getElementById('achievements-list');
    const achievementsListMobile = document.getElementById('achievements-list-mobile');
    
    if (!achievementsList && !achievementsListMobile) return;

    const totalRides = rides.length;
    const completedRides = rides.filter(ride => !ride.isActive).length;

    // Update achievements based on user activity
    const achievements = [];

    if (totalRides > 0) {
        achievements.push({
            icon: 'fas fa-star',
            color: '#fbbf24',
            text: translateText('achievements.firstRide')
        });
    }

    if (completedRides >= 5) {
        achievements.push({
            icon: 'fas fa-users',
            color: '#10b981',
            text: translateText('achievements.fiveRides')
        });
    }

    if (completedRides >= 10) {
        achievements.push({
            icon: 'fas fa-trophy',
            color: '#059669',
            text: translateText('achievements.tenRides')
        });
    }

    // Function to populate achievements list
    function populateAchievementsList(container) {
        if (!container) return;
        
        container.innerHTML = '';
        achievements.forEach((achievement, index) => {
            const achievementElement = document.createElement('div');
            achievementElement.className = 'achievement-item';
            achievementElement.style.opacity = '0';
            achievementElement.style.transform = 'scale(0.8)';

            achievementElement.innerHTML = `
                <i class="${achievement.icon}" style="color: ${achievement.color};"></i>
                <span>${achievement.text}</span>
            `;

            container.appendChild(achievementElement);

            setTimeout(() => {
                achievementElement.style.transition = 'all 0.3s ease';
                achievementElement.style.opacity = '1';
                achievementElement.style.transform = 'scale(1)';
            }, index * 200);
        });
    }

    // Populate both desktop and mobile achievements lists
    populateAchievementsList(achievementsList);
    populateAchievementsList(achievementsListMobile);
}

function switchTab(tabType) {
    // Update active tab
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelector(`[data-tab="${tabType}"]`).classList.add('active');
    
    // Filter rides
    const rideItems = document.querySelectorAll('.user-ride-item');
    const noRides = document.getElementById('no-rides');
    const noRidesTitle = document.getElementById('no-rides-title');
    const noRidesDescription = document.getElementById('no-rides-description');
    
    let visibleRides = 0;
    
    rideItems.forEach(ride => {
        const isActive = ride.querySelector('.user-ride-status').classList.contains('active');
        const shouldShow = (tabType === 'active' && isActive) || (tabType === 'completed' && !isActive);
        
        if (shouldShow) {
            ride.style.display = 'block';
            visibleRides++;
        } else {
            ride.style.display = 'none';
        }
    });
    
    // Show/hide no rides message
    if (visibleRides === 0) {
        noRides.style.display = 'block';
        
        // Verificăm dacă suntem pe profilul propriu sau al altui utilizator
        const pathSegments = window.location.pathname.split('/');
        const targetUserId = pathSegments.length > 2 && pathSegments[1] === 'profile' ? pathSegments[2] : null;
        const isOwnProfile = !targetUserId || targetUserId === 'edit-profile';
        
        if (tabType === 'active') {
            noRidesTitle.textContent = isOwnProfile ? translateText('rides.noRides') : translateText('rides.noRidesOther');
            noRidesDescription.textContent = isOwnProfile ? translateText('rides.noRidesDesc') : translateText('rides.noRidesDescOther');
        } else {
            noRidesTitle.textContent = isOwnProfile ? translateText('rides.noRides') : translateText('rides.noRidesOther');
            noRidesDescription.textContent = isOwnProfile ? translateText('rides.noRidesDesc') : translateText('rides.noRidesDescOther');
        }
    } else {
        noRides.style.display = 'none';
    }
}

function formatDate(dateString) {
    if (!dateString) return translateText('info.notSpecified') || 'Nu specificat';
    
    const date = new Date(dateString);
    const currentLang = getCurrentLanguage();
    const locale = currentLang === 'ru' ? 'ru-RU' : 'ro-RO';
    
    return date.toLocaleDateString(locale, {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'success' ? '#10b981' : type === 'error' ? '#ef4444' : '#3b82f6'};
        color: white;
        padding: 1rem 1.5rem;
        border-radius: 8px;
        box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
        z-index: 1000;
        font-size: 0.9rem;
        font-weight: 500;
        max-width: 300px;
        word-wrap: break-word;
    `;
    
    notification.innerHTML = `
        ${message}
        <button class="notification-close" style="
            background: none;
            border: none;
            color: white;
            margin-left: 1rem;
            cursor: pointer;
            font-size: 1.2rem;
            opacity: 0.8;
        " onclick="this.parentElement.remove()">&times;</button>
    `;
    
    document.body.appendChild(notification);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (notification.parentElement) {
            notification.remove();
        }
    }, 5000);
}

// Ride action functions
function viewRide(rideId) {
    const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
    window.location.href = `/${currentLang}/ride/${rideId}`;
}

function editRide(rideId) {
    const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
    window.location.href = `/${currentLang}/edit-ride?id=${rideId}`;
}

function deleteRide(rideId) {
    if (confirm(translateText('rides.deleteConfirm'))) {
        fetch(`/api/rides/${rideId}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.ok) {
                showNotification(translateText('rides.deleteSuccess'), 'success');
                // Reload rides - verificăm dacă suntem pe profilul propriu
                const pathSegments = window.location.pathname.split('/');
                const targetUserId = pathSegments.length > 2 && pathSegments[1] === 'profile' ? pathSegments[2] : null;
                
                if (!targetUserId || targetUserId === 'edit-profile') {
                    loadUserRides(); // Reload rides pentru profilul propriu
                } else {
                    loadSpecificUserRides(targetUserId); // Reload rides pentru profilul specific
                }
            } else {
                showNotification(translateText('rides.deleteError'), 'error');
            }
        })
        .catch(error => {
            console.error('Error deleting ride:', error);
            showNotification(translateText('rides.deleteError'), 'error');
        });
    }
}

// Rating System Functions
function initializeRatingSystem() {
    // Setup star rating hover effects
    setupStarRatingEffects();
    
    // Setup login redirect functionality
    setupLoginRedirect();
}

function setupStarRatingEffects() {
    const starLabels = document.querySelectorAll('.star-label');
    
    starLabels.forEach((label, index) => {
        label.addEventListener('mouseenter', function() {
                    // Highlight stars from current to first (left to right)
        // Stars are now ordered 1,2,3,4,5 in HTML, so we can use the index directly
        for (let i = 0; i <= index; i++) {
            starLabels[i].style.color = '#fbbf24';
        }
        });
        
        label.addEventListener('mouseleave', function() {
            // Reset all stars to default color, but keep selected rating
            const selectedRating = document.querySelector('input[name="rating"]:checked');
            if (selectedRating) {
                const ratingValue = parseInt(selectedRating.value);
                starLabels.forEach((star, i) => {
                    // Stars are now ordered 1,2,3,4,5 in HTML, so we can use the index directly
                    if (i < ratingValue) {
                        star.style.color = '#fbbf24';
                    } else {
                        star.style.color = '#d1d5db';
                    }
                });
            } else {
                starLabels.forEach(star => {
                    star.style.color = '#d1d5db';
                });
            }
        });
        
        // Add click event to select rating
        label.addEventListener('click', function() {
            const ratingValue = index + 1;
            const radioInput = document.getElementById(`star${ratingValue}`);
            
            // Uncheck all radio inputs
            document.querySelectorAll('input[name="rating"]').forEach(input => {
                input.checked = false;
            });
            
            // Check the selected rating
            radioInput.checked = true;
            
            // Update star colors to show selected rating (left to right)
            starLabels.forEach((star, i) => {
                // Stars are now ordered 1,2,3,4,5 in HTML, so we can use the index directly
                if (i < ratingValue) {
                    star.style.color = '#fbbf24';
                } else {
                    star.style.color = '#d1d5db';
                }
            });
        });
    });
}

function setupLoginRedirect() {
    const loginBtn = document.getElementById('login-redirect-btn');
    if (loginBtn) {
        loginBtn.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Save current profile URL for redirect after login
            const currentProfileUrl = window.location.pathname;
            sessionStorage.setItem('redirectAfterLogin', currentProfileUrl);
            
            console.log('🔗 Saving redirect URL:', currentProfileUrl);
            
            // Redirect to login page
            const currentLang = document.querySelector('.current-lang')?.textContent === 'RO' ? 'ro' : 'ru';
            window.location.href = '/' + currentLang + '/login';
        });
    }
}

function loadUserRatingData(userId) {
    console.log('🔍 loadUserRatingData called for userId:', userId);
    
    // First check if user is authenticated
    fetch('/api/auth/check')
        .then(response => response.json())
        .then(authData => {
            console.log('🔍 Auth check result:', authData);
            
            if (authData.authenticated) {
                const currentUser = authData.user;
                console.log('✅ User authenticated:', currentUser);
                
                // Check if user is trying to rate themselves
                if (currentUser.id == userId) {
                    console.log('❌ User cannot rate themselves, hiding rating form but showing feedback');
                    // Hide the rating form but keep the section visible for feedback display
                    const ratingFormContainer = document.getElementById('rating-form-container');
                    const loginRequiredMessage = document.getElementById('login-required-message');
                    
                    if (ratingFormContainer) ratingFormContainer.style.display = 'none';
                    if (loginRequiredMessage) loginRequiredMessage.style.display = 'none';
                    
                    // Don't return here, continue to load rating data for display
                } else {
                    // Show rating form for other users
                    const ratingFormContainer = document.getElementById('rating-form-container');
                    if (ratingFormContainer) ratingFormContainer.style.display = 'block';
                }
                
                // Show rating section and form container for authenticated users
                const ratingSection = document.getElementById('rating-section');
                const ratingFormContainer = document.getElementById('rating-form-container');
                const loginRequiredMessage = document.getElementById('login-required-message');
                const ownProfileRatings = document.getElementById('own-profile-ratings');
                const otherProfileRatings = document.getElementById('other-profile-ratings');
                
                if (ratingSection) ratingSection.style.display = 'block';
                
                // Check if we're on own profile or other user's profile
                const pathSegments = window.location.pathname.split('/');
                
                // Handle both language-prefixed URLs (/ro/profile/123) and non-prefixed URLs (/profile/123)
                let targetUserId = null;
                if (pathSegments.length > 3 && pathSegments[2] === 'profile') {
                    // Language-prefixed URL: /ro/profile/123 or /ru/profile/123
                    targetUserId = pathSegments[3];
                } else if (pathSegments.length > 2 && pathSegments[1] === 'profile') {
                    // Non-prefixed URL: /profile/123
                    targetUserId = pathSegments[2];
                }
                const isOwnProfile = !targetUserId || targetUserId === 'edit-profile';
                
                if (isOwnProfile) {
                    if (ownProfileRatings) ownProfileRatings.style.display = 'block';
                    if (otherProfileRatings) otherProfileRatings.style.display = 'none';
                    console.log('✅ Own profile ratings section shown');
                } else {
                    if (ownProfileRatings) ownProfileRatings.style.display = 'none';
                    if (otherProfileRatings) otherProfileRatings.style.display = 'block';
                    if (ratingFormContainer) ratingFormContainer.style.display = 'block';
                    if (loginRequiredMessage) loginRequiredMessage.style.display = 'none';
                    console.log('✅ Other profile ratings section shown');
                }
                
                // Load rating summary
                return fetch(`/api/ratings/user/${userId}`);
            } else {
                console.log('❌ User not authenticated');
                throw new Error('User not authenticated');
            }
        })
        .then(response => {
            if (response && response.ok) {
                return response.json();
            } else if (response) {
                throw new Error('Failed to load rating data');
            }
        })
        .then(data => {
            if (data && data.success) {
                const averageRating = data.averageRating || 0;
                const totalRatings = data.totalRatings || 0;
                
                // Update rating display for own profile
                const pathSegments = window.location.pathname.split('/');
                
                // Handle both language-prefixed URLs (/ro/profile/123) and non-prefixed URLs (/profile/123)
                let targetUserId = null;
                if (pathSegments.length > 3 && pathSegments[2] === 'profile') {
                    // Language-prefixed URL: /ro/profile/123 or /ru/profile/123
                    targetUserId = pathSegments[3];
                } else if (pathSegments.length > 2 && pathSegments[1] === 'profile') {
                    // Non-prefixed URL: /profile/123
                    targetUserId = pathSegments[2];
                }
                const isOwnProfile = !targetUserId || targetUserId === 'edit-profile';
                
                if (isOwnProfile) {
                    updateOwnProfileRatingDisplay(averageRating, totalRatings);
                }
                
                displayUserRatings(data.ratings);
            } else {
                // Set default values if no rating data
                const pathSegments = window.location.pathname.split('/');
                
                // Handle both language-prefixed URLs (/ro/profile/123) and non-prefixed URLs (/profile/123)
                let targetUserId = null;
                if (pathSegments.length > 3 && pathSegments[2] === 'profile') {
                    // Language-prefixed URL: /ro/profile/123 or /ru/profile/123
                    targetUserId = pathSegments[3];
                } else if (pathSegments.length > 2 && pathSegments[1] === 'profile') {
                    // Non-prefixed URL: /profile/123
                    targetUserId = pathSegments[2];
                }
                const isOwnProfile = !targetUserId || targetUserId === 'edit-profile';
                
                if (isOwnProfile) {
                    updateOwnProfileRatingDisplay(0, 0);
                }
            }
            
            // Check if current user has already rated this user
            return checkCurrentUserRating(userId);
        })
        .catch(error => {
            console.log('❌ User not authenticated or error occurred:', error);
            
            // Show login required message for non-authenticated users
            const ratingSection = document.getElementById('rating-section');
            const ratingFormContainer = document.getElementById('rating-form-container');
            const loginRequiredMessage = document.getElementById('login-required-message');
            const ownProfileRatings = document.getElementById('own-profile-ratings');
            const otherProfileRatings = document.getElementById('other-profile-ratings');
            
            if (ratingSection) ratingSection.style.display = 'block';
            
            // Check if we're on own profile or other user's profile
            const pathSegments = window.location.pathname.split('/');
            
            // Handle both language-prefixed URLs (/ro/profile/123) and non-prefixed URLs (/profile/123)
            let targetUserId = null;
            if (pathSegments.length > 3 && pathSegments[2] === 'profile') {
                // Language-prefixed URL: /ro/profile/123 or /ru/profile/123
                targetUserId = pathSegments[3];
            } else if (pathSegments.length > 2 && pathSegments[1] === 'profile') {
                // Non-prefixed URL: /profile/123
                targetUserId = pathSegments[2];
            }
            const isOwnProfile = !targetUserId || targetUserId === 'edit-profile';
            
            if (isOwnProfile) {
                if (ownProfileRatings) ownProfileRatings.style.display = 'block';
                if (otherProfileRatings) otherProfileRatings.style.display = 'none';
                console.log('✅ Own profile ratings section shown for non-authenticated user');
            } else {
                if (ownProfileRatings) ownProfileRatings.style.display = 'none';
                if (otherProfileRatings) otherProfileRatings.style.display = 'block';
                if (ratingFormContainer) ratingFormContainer.style.display = 'none';
                if (loginRequiredMessage) loginRequiredMessage.style.display = 'block';
                console.log('✅ Login required message shown for non-authenticated user');
            }
            
            // Still try to load rating summary for display
            return fetch(`/api/ratings/user/${userId}`);
        })
        .then(response => {
            if (response && response.ok) {
                return response.json();
            }
        })
        .then(data => {
            if (data && data.success) {
                const averageRating = data.averageRating || 0;
                const totalRatings = data.totalRatings || 0;
                // updateRatingDisplay(averageRating, totalRatings); // Removed as per edit hint
                displayUserRatings(data.ratings);
            } else {
                // Set default values if no rating data
                // updateRatingDisplay(0, 0); // Removed as per edit hint
            }
        })
        .catch(error => {
            console.error('Error loading rating data:', error);
            // No need to update rating display since we removed that section
        });
}

function updateOwnProfileRatingDisplay(averageRating, totalRatings) {
    console.log('🔍 updateOwnProfileRatingDisplay called with:', averageRating, totalRatings);
    
    const ratingNumber = document.getElementById('own-rating-number');
    const starsDisplay = document.getElementById('own-stars-display');
    const ratingCount = document.getElementById('own-rating-count');
    
    if (ratingNumber) {
        ratingNumber.textContent = averageRating.toFixed(1);
    }
    
    if (starsDisplay) {
        starsDisplay.innerHTML = '';
        for (let i = 1; i <= 5; i++) {
            if (i <= averageRating) {
                starsDisplay.innerHTML += '<i class="fas fa-star"></i>';
            } else if (i - averageRating < 1) {
                starsDisplay.innerHTML += '<i class="fas fa-star-half-alt"></i>';
            } else {
                starsDisplay.innerHTML += '<i class="far fa-star"></i>';
            }
        }
    }
    
    if (ratingCount) {
        ratingCount.textContent = `(${totalRatings} ${translateText('rating.ratings')})`;
    }
    
    console.log('✅ Own profile rating display updated');
}

function displayUserRatings(ratings) {
    console.log('🔍 displayUserRatings called with ratings:', ratings);
    
    // Check if we're on own profile or other user's profile
    const pathSegments = window.location.pathname.split('/');
    
    // Handle both language-prefixed URLs (/ro/profile/123) and non-prefixed URLs (/profile/123)
    let targetUserId = null;
    if (pathSegments.length > 3 && pathSegments[2] === 'profile') {
        // Language-prefixed URL: /ro/profile/123 or /ru/profile/123
        targetUserId = pathSegments[3];
    } else if (pathSegments.length > 2 && pathSegments[1] === 'profile') {
        // Non-prefixed URL: /profile/123
        targetUserId = pathSegments[2];
    }
    const isOwnProfile = !targetUserId || targetUserId === 'edit-profile';
    
    // Choose the correct elements based on profile type
    const ratingsList = isOwnProfile ? document.getElementById('own-ratings-list') : document.getElementById('ratings-list');
    const userRatings = isOwnProfile ? document.getElementById('own-user-ratings') : document.getElementById('user-ratings');
    
    if (!ratingsList || !userRatings) {
        console.error('❌ Ratings elements not found for', isOwnProfile ? 'own profile' : 'other profile');
        return;
    }
    
    if (!ratings || ratings.length === 0) {
        userRatings.style.display = 'none';
        console.log('✅ No ratings to display, hiding ratings section');
        return;
    }
    
    // Show ratings section
    userRatings.style.display = 'block';
    
    // Clear existing ratings
    ratingsList.innerHTML = '';
    
    // Add each rating
    ratings.forEach(rating => {
        const ratingItem = document.createElement('div');
        ratingItem.className = 'rating-item';
        
        const stars = [];
        for (let i = 1; i <= 5; i++) {
            if (i <= rating.rating) {
                stars.push('<i class="fas fa-star"></i>');
            } else {
                stars.push('<i class="far fa-star"></i>');
            }
        }
        
        ratingItem.innerHTML = `
            <div class="rating-info">
                <div class="rating-stars">${stars.join('')}</div>
                ${rating.comment ? `<div class="rating-comment">"${rating.comment}"</div>` : ''}
            </div>
            <div class="rating-date">${formatDate(rating.createdAt)}</div>
        `;
        
        ratingsList.appendChild(ratingItem);
    });
    
    console.log(`✅ Displayed ${ratings.length} ratings for ${isOwnProfile ? 'own profile' : 'other profile'}`);
}

function checkCurrentUserRating(ratedUserId) {
    console.log('🔍 checkCurrentUserRating called for ratedUserId:', ratedUserId);
    
    // First check if user is authenticated
    fetch('/api/auth/check')
        .then(response => response.json())
        .then(authData => {
            if (!authData.authenticated) {
                console.log('❌ User not authenticated, cannot check rating');
                return;
            }
            
            console.log('✅ User authenticated, checking rating...');
            
            return fetch(`/api/ratings/check/${ratedUserId}`);
        })
        .then(response => {
            if (response && response.ok) {
                return response.json();
            } else if (response) {
                throw new Error('Failed to check rating');
            }
        })
        .then(data => {
            if (data && data.success) {
                if (data.hasRated) {
                    console.log('✅ User has already rated, showing update form');
                    showRatingUpdate(data.existingRating);
                } else {
                    console.log('✅ User has not rated, showing rating form');
                    showRatingForm();
                }
            } else {
                console.log('❌ Rating check failed, showing rating form');
                showRatingForm();
            }
        })
        .catch(error => {
            console.error('❌ Error checking rating:', error);
            showRatingForm();
        });
}

function showRatingForm() {
    console.log('🔍 showRatingForm called');
    
    const ratingForm = document.getElementById('rating-form');
    const ratingUpdate = document.getElementById('rating-update');
    
    if (ratingForm) {
        ratingForm.style.display = 'block';
        console.log('✅ Rating form shown');
    }
    if (ratingUpdate) {
        ratingUpdate.style.display = 'none';
        console.log('✅ Rating update hidden');
    }
    
    // Reset rating form
    const commentField = document.getElementById('rating-comment');
    if (commentField) commentField.value = '';
    
    document.querySelectorAll('input[name="rating"]').forEach(input => {
        input.checked = false;
    });
    
    // Reset star colors
    const starLabels = document.querySelectorAll('.star-label');
    starLabels.forEach(star => {
        star.style.color = '#d1d5db';
    });
    
    console.log('✅ Rating form reset to default state');
    
    // Change button text and action to submit new rating
    const submitBtn = document.getElementById('submit-rating-btn');
    if (submitBtn) {
        submitBtn.innerHTML = '<i class="fas fa-paper-plane"></i> ' + translateText('rating.submitRating');
        submitBtn.onclick = submitRating;
    }
    
    console.log('✅ Rating form reset and ready');
}

function showRatingUpdate(existingRating) {
    console.log('🔍 showRatingUpdate called with existingRating:', existingRating);
    
    // Show the rating form without resetting it
    const ratingForm = document.getElementById('rating-form');
    const ratingUpdate = document.getElementById('rating-update');
    
    if (ratingForm) {
        ratingForm.style.display = 'block';
        console.log('✅ Rating form shown');
    }
    if (ratingUpdate) {
        ratingUpdate.style.display = 'none';
        console.log('✅ Rating update hidden');
    }
    
            // Pre-fill the form with existing rating data
        if (existingRating) {
            console.log('🔍 Pre-filling form with existing rating:', existingRating);
            
            // Set the rating
            const ratingInput = document.getElementById(`star${existingRating.rating}`);
            if (ratingInput) {
                ratingInput.checked = true;
                console.log('✅ Set rating input to:', existingRating.rating);
            }
            
            // Set the comment
            const commentField = document.getElementById('rating-comment');
            if (commentField && existingRating.comment) {
                commentField.value = existingRating.comment;
                console.log('✅ Set comment to:', existingRating.comment);
            }
            
            // Update star colors to show the current rating
            const starLabels = document.querySelectorAll('.star-label');
            console.log('🔍 Updating star colors for rating:', existingRating.rating);
            starLabels.forEach((star, i) => {
                // Stars are now ordered 1,2,3,4,5 in HTML, so we can use the index directly
                if (i < existingRating.rating) {
                    star.style.color = '#fbbf24';
                    console.log(`✅ Colored star ${i + 1} yellow`);
                } else {
                    star.style.color = '#d1d5db';
                    console.log(`✅ Colored star ${i + 1} gray`);
                }
            });
        
        // Change submit button text to indicate this is an update
        const submitBtn = document.getElementById('submit-rating-btn');
        if (submitBtn) {
            submitBtn.innerHTML = '<i class="fas fa-paper-plane"></i> ' + translateText('rating.submitRating');
        }
        
        console.log('✅ Rating form pre-filled with existing rating data');
    }
}

function submitRating() {
    // Get rating value
    const ratingInput = document.querySelector('input[name="rating"]:checked');
    if (!ratingInput) {
        showNotification(translateText('rating.selectRating'), 'error');
        return;
    }
    
    const rating = parseInt(ratingInput.value);
    const comment = document.getElementById('rating-comment').value;
    
    // Get the user ID from the URL
    const pathSegments = window.location.pathname.split('/');
    
    // Handle both language-prefixed URLs (/ro/profile/123) and non-prefixed URLs (/profile/123)
    let ratedUserId = null;
    if (pathSegments.length > 3 && pathSegments[2] === 'profile') {
        // Language-prefixed URL: /ro/profile/123 or /ru/profile/123
        ratedUserId = pathSegments[3];
    } else if (pathSegments.length > 2 && pathSegments[1] === 'profile') {
        // Non-prefixed URL: /profile/123
        ratedUserId = pathSegments[2];
    }
    
    if (!ratedUserId) {
        showNotification('Eroare: ID utilizator invalid!', 'error');
        return;
    }
    
    // Convert to number to ensure proper type
    ratedUserId = parseInt(ratedUserId);
    if (isNaN(ratedUserId)) {
        showNotification('Eroare: ID utilizator invalid!', 'error');
        return;
    }
    
    // First check if user is authenticated
    fetch('/api/auth/check')
        .then(response => response.json())
        .then(authData => {
            if (!authData.authenticated) {
                throw new Error('User not authenticated');
            }
            
            return fetch('/api/ratings/rate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    ratedUserId: ratedUserId.toString(),
                    rating: rating.toString(),
                    comment: comment || ''
                })
            });
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
        })
        .then(data => {
            const message = data.message || 'Rating-ul a fost procesat cu succes!';
            showNotification(message, 'success');
            
            // Reload the entire page after successful rating submission
            window.location.reload();
        })
        .catch(error => {
            if (error.message.includes('not authenticated')) {
                showNotification('Trebuie să fiți logat pentru a pune un rating!', 'error');
            } else {
                showNotification('Eroare la procesarea rating-ului: ' + error.message, 'error');
            }
        });
}

/**
 * Setează numărul de telefon cu funcționalitatea de copiere
 * @param {string} phoneNumber - Numărul de telefon de afișat
 */
function setPhoneWithCopy(phoneNumber) {
    const phoneElement = document.getElementById('phone');
    const copyButton = document.getElementById('copy-phone-btn');
    
    if (phoneElement) {
        phoneElement.textContent = phoneNumber;
    }
    
    if (copyButton && phoneNumber !== 'Nu specificat') {
        copyButton.style.display = 'inline-flex';
        copyButton.onclick = function() {
            copyToClipboard(phoneNumber);
        };
    } else if (copyButton) {
        copyButton.style.display = 'none';
    }
}

/**
 * Copiază textul în clipboard
 * @param {string} text - Textul de copiat
 */
function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(function() {
        // Show success feedback
        const copyButtons = document.querySelectorAll('.copy-phone-btn');
        copyButtons.forEach(btn => {
            if (btn.textContent.includes(text) || btn.getAttribute('data-phone') === text) {
                btn.classList.add('copied');
                btn.innerHTML = '<i class="fas fa-check"></i>';
                
                setTimeout(() => {
                    btn.classList.remove('copied');
                    btn.innerHTML = '<i class="fas fa-copy"></i>';
                }, 2000);
            }
        });
        
        showNotification('Numărul de telefon a fost copiat în clipboard!', 'success');
    }).catch(function(err) {
        console.error('Eroare la copierea în clipboard:', err);
        showNotification('Eroare la copierea numărului de telefon', 'error');
    });
}