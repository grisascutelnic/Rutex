document.addEventListener('DOMContentLoaded', function() {
    checkAuthStatus();
    // Nu mai apelăm initializeMobileMenu() aici pentru că se va apela în checkAuthStatus()
});

// Funcție pentru inițializarea meniului mobil care poate fi apelată din alte scripturi
function initializeMobileMenu() {
    const hamburger = document.querySelector('.hamburger');
    const navMenu = document.querySelector('.nav-menu');
    const navAuth = document.querySelector('.nav-auth');
    
    if (hamburger && navMenu && navAuth) {
        // Eliminăm event listener-urile existente pentru a evita duplicarea
        const newHamburger = hamburger.cloneNode(true);
        hamburger.parentNode.replaceChild(newHamburger, hamburger);
        
        newHamburger.addEventListener('click', function(e) {
            e.stopPropagation(); // Previne propagarea click-ului
            newHamburger.classList.toggle('active');
            navMenu.classList.toggle('active');
            navAuth.classList.toggle('active');

            const notificationDropdown = document.getElementById('notification-dropdown');
            if (notificationDropdown) {
                notificationDropdown.classList.remove('open');
            }
        });
        
        // Închide meniul când se face click în afara lui
        document.addEventListener('click', function(e) {
            if (e.target.closest('.notification-wrapper')) {
                return;
            }
            const isClickInsideNavbar = e.target.closest('.navbar');
            const isClickOnHamburger = e.target.closest('.hamburger');
            
            if (!isClickInsideNavbar && !isClickOnHamburger) {
                // Click în afara navbar-ului, închide meniul
                newHamburger.classList.remove('active');
                navMenu.classList.remove('active');
                navAuth.classList.remove('active');
                const notificationDropdown = document.getElementById('notification-dropdown');
                if (notificationDropdown) {
                    notificationDropdown.classList.remove('open');
                }
            }
        });
        
        // Închide meniul când se face click pe un link din meniu
        const navLinks = document.querySelectorAll('.nav-menu a, .nav-auth a, .nav-auth button');
        navLinks.forEach(link => {
            link.addEventListener('click', function() {
                if (this.classList.contains('notification-bell') || this.closest('.notification-wrapper')) {
                    return;
                }
                newHamburger.classList.remove('active');
                navMenu.classList.remove('active');
                navAuth.classList.remove('active');
            });
        });
    }
}



function checkAuthStatus() {
    console.log('Checking authentication status...');
    
    fetch('/api/auth/check')
        .then(response => {
            console.log('Auth check response status:', response.status);
            if (response.ok) {
                return response.json();
            } else {
                console.log('Auth check failed, status:', response.status);
                return { authenticated: false };
            }
        })
        .then(data => {
            console.log('Auth check response data:', data);
        if (data.authenticated && data.user) {
            console.log('User authenticated:', data.user.email);
            updateNavbarForLoggedInUser(data.user);
            initializeNotifications();
        } else {
            console.log('No authenticated user found');
            updateNavbarForLoggedOutUser();
        }
        })
        .catch(error => {
            console.error('Error checking auth status:', error);
            updateNavbarForLoggedOutUser();
        })
        .finally(() => {
            // Always initialize mobile menu after auth check, regardless of result
            initializeMobileMenu();
        });
}

function updateNavbarForLoggedInUser(user) {
    const navAuth = document.getElementById('nav-auth');
    if (navAuth) {
        // Verificăm rolurile utilizatorului
        const isAdmin = user.roles && user.roles.some(role => role.name === 'ROLE_ADMIN');
        const isMod = user.roles && user.roles.some(role => role.name === 'ROLE_MOD');
        
        // Doar adăugăm funcționalitatea de logout, nu rescrim tot navbar-ul
        const logoutBtn = document.getElementById('logout-btn');
        if (logoutBtn) {
            logoutBtn.onclick = logout;
        }
        
        // Dacă navbar-ul nu există (utilizator neautentificat), îl creăm
        if (!navAuth.querySelector('.nav-user')) {
            // Create role indicator if user has special roles
            let roleIndicator = '';
            if (isAdmin) {
                roleIndicator = '<span class="role-indicator admin">ADMIN</span>';
            } else if (isMod) {
                roleIndicator = '<span class="role-indicator moderator">MODERATOR</span>';
            }
            
            // Pentru navbar, folosim doar clasa de bază user-name (fără culori)
            const currentLang = getCurrentLanguage();
            const profileUrl = '/' + currentLang + '/profile';
            const usersUrl = '/' + currentLang + '/users';
            
            // Obținem textele traduse din navbar-ul existent
            const logoutButton = navAuth.querySelector('.btn-logout');
            const adminLink = navAuth.querySelector('.admin-link');
            
            let logoutText = 'Deconectare';
            let adminText = 'Administrare';
            
            if (logoutButton) {
                logoutText = logoutButton.textContent || logoutButton.innerText || 'Deconectare';
            }
            if (adminLink) {
                adminText = adminLink.textContent || adminLink.innerText || 'Administrare';
            }
            
            navAuth.innerHTML = `
                <div class="nav-user">
                    <a href="${profileUrl}" class="nav-profile" title="Profilul meu">
                        <span class="user-identity">
                            <i class="fas fa-user-circle"></i>
                            <span class="user-name">${user.firstName}</span>
                        </span>
                        ${roleIndicator}
                    </a>
                    ${isAdmin ? `
                        <a href="${usersUrl}" class="nav-link admin-link" title="Administrare Utilizatori">
                            <i class="fas fa-users-cog"></i> ${adminText}
                        </a>
                    ` : ''}
                    <button class="btn-logout" onclick="logout()">
                        <i class="fas fa-sign-out-alt"></i>
                        ${logoutText}
                    </button>
                </div>
            `;
        }
    }

    ensureNotificationWrapper();
}

function updateNavbarForLoggedOutUser() {
    const navAuth = document.getElementById('nav-auth');
    if (navAuth) {
        // Detectăm limba curentă din URL sau din elementul de limbă
        const currentLang = getCurrentLanguage();
        const loginUrl = '/' + currentLang + '/login';
        
        // Obținem textul tradus din atributul data-original-text sau folosim textul curent
        const loginButton = navAuth.querySelector('.btn-login');
        let loginText = 'Conectare'; // fallback
        
        if (loginButton) {
            // Păstrăm textul tradus existent
            loginText = loginButton.textContent || loginButton.innerText || 'Conectare';
        }
        
        navAuth.innerHTML = `
            <a href="${loginUrl}" class="btn-login" id="login-btn" onclick="saveCurrentUrlAndRedirect(event, '${loginUrl}')">${loginText}</a>
        `;
    }

    const slot = document.getElementById('notification-slot');
    if (slot) {
        slot.innerHTML = '';
    }
}

function getCurrentLanguage() {
    // Încercăm să detectăm limba din URL
    const path = window.location.pathname;
    if (path.startsWith('/ru/') || path === '/ru') {
        return 'ru';
    } else if (path.startsWith('/ro/') || path === '/ro') {
        return 'ro';
    }
    
    // Fallback: detectăm din elementul de limbă
    const currentLangElement = document.querySelector('.current-lang');
    if (currentLangElement) {
        return currentLangElement.textContent === 'RO' ? 'ro' : 'ru';
    }
    
    return 'ro'; // default
}

function getNotificationTexts() {
    const titleEl = document.querySelector('.notification-title');
    const emptyEl = document.querySelector('.notification-empty');
    const markAllEl = document.querySelector('.notification-mark-all');
    const pushTextEl = document.querySelector('.notification-push-text');
    const pushButtonEl = document.querySelector('.notification-push-button');
    const pushStatusEl = document.querySelector('.notification-push-status');

    return {
        title: titleEl ? (titleEl.textContent || titleEl.innerText) : 'Notificari',
        empty: emptyEl ? (emptyEl.textContent || emptyEl.innerText) : 'Nu ai notificari',
        markAll: markAllEl ? (markAllEl.textContent || markAllEl.innerText) : 'Marcheaza toate',
        pushPrompt: pushTextEl ? (pushTextEl.textContent || pushTextEl.innerText) : 'Activeaza notificarile pe telefon',
        pushEnable: pushButtonEl ? (pushButtonEl.textContent || pushButtonEl.innerText) : 'Activeaza',
        pushDisable: pushButtonEl?.getAttribute('data-disable-text') || 'Dezactiveaza',
        pushEnabled: pushStatusEl?.getAttribute('data-enabled-text') || 'Notificarile sunt active.',
        pushBlocked: pushStatusEl?.getAttribute('data-blocked-text') || 'Notificarile sunt blocate in browser.'
    };
}

function ensureNotificationWrapper() {
    const slot = document.getElementById('notification-slot');
    if (!slot) {
        return;
    }

    const existingWrapper = document.querySelector('.notification-wrapper');
    if (existingWrapper) {
        existingWrapper.classList.add('notification-wrapper--nav');
        const label = existingWrapper.querySelector('.notification-label');
        if (label) {
            label.remove();
        }
        placeNotificationWrapper(existingWrapper);
        return;
    }

    const notificationTexts = getNotificationTexts();
    slot.innerHTML = `
        <div class="notification-wrapper notification-wrapper--nav">
            <button class="notification-bell" id="notification-bell" type="button" aria-label="${notificationTexts.title}">
                <i class="fas fa-bell"></i>
                <span class="notification-badge" id="notification-badge" hidden>0</span>
            </button>
            <div class="notification-dropdown" id="notification-dropdown">
                <div class="notification-header">
                    <span class="notification-title">${notificationTexts.title}</span>
                    <button class="notification-mark-all" id="notification-mark-all" type="button">
                        ${notificationTexts.markAll}
                    </button>
                </div>
                <div class="notification-push" id="notification-push" hidden>
                    <div class="notification-push-text" id="notification-push-text">${notificationTexts.pushPrompt}</div>
                    <button class="notification-push-button" id="notification-push-button" type="button"
                            data-disable-text="${notificationTexts.pushDisable}"
                            data-enable-text="${notificationTexts.pushEnable}">
                        ${notificationTexts.pushEnable}
                    </button>
                </div>
                <div class="notification-push-status" id="notification-push-status"
                     data-enabled-text="${notificationTexts.pushEnabled}"
                     data-blocked-text="${notificationTexts.pushBlocked}"
                     hidden>${notificationTexts.pushEnabled}</div>
                <div class="notification-list" id="notification-list"></div>
                <div class="notification-empty" id="notification-empty">${notificationTexts.empty}</div>
            </div>
        </div>
    `;
    const wrapper = slot.querySelector('.notification-wrapper');
    if (wrapper) {
        placeNotificationWrapper(wrapper);
    }
}

function placeNotificationWrapper(wrapper) {
    const isMobile = window.matchMedia('(max-width: 768px)').matches;
    if (isMobile) {
        const slot = document.getElementById('notification-slot');
        if (slot && wrapper.parentElement !== slot) {
            slot.appendChild(wrapper);
        }
        return;
    }

    const navUser = document.querySelector('.nav-auth .nav-user');
    if (!navUser) {
        return;
    }

    const logoutButton = navUser.querySelector('.btn-logout');
    if (logoutButton) {
        navUser.insertBefore(wrapper, logoutButton);
    } else {
        navUser.appendChild(wrapper);
    }
}

window.addEventListener('resize', () => {
    const wrapper = document.querySelector('.notification-wrapper');
    if (wrapper) {
        placeNotificationWrapper(wrapper);
    }
});

function initializeNotifications() {
    const bell = document.getElementById('notification-bell');
    const dropdown = document.getElementById('notification-dropdown');
    const list = document.getElementById('notification-list');
    const empty = document.getElementById('notification-empty');
    const badge = document.getElementById('notification-badge');
    const markAll = document.getElementById('notification-mark-all');
    const pushPrompt = document.getElementById('notification-push');
    const pushButton = document.getElementById('notification-push-button');
    const pushStatus = document.getElementById('notification-push-status');

    if (!bell || !dropdown || !list || !empty || !badge) {
        return;
    }

    const currentLang = getCurrentLanguage();
    const wrapper = bell.closest('.notification-wrapper');

    const updateBadge = (count) => {
        const numericCount = Number(count) || 0;
        if (numericCount > 0) {
            badge.hidden = false;
            badge.removeAttribute('hidden');
            badge.classList.remove('is-hidden');
            badge.textContent = numericCount > 99 ? '99+' : numericCount;
        } else {
            badge.hidden = true;
            badge.setAttribute('hidden', '');
            badge.classList.add('is-hidden');
            badge.textContent = '0';
        }
    };

    const renderNotifications = (items) => {
        list.innerHTML = '';
        if (!items || items.length === 0) {
            empty.style.display = 'block';
            return;
        }

        empty.style.display = 'none';
        items.forEach(item => {
            const createdAt = item.createdAt ? new Date(item.createdAt) : null;
            const dateLabel = createdAt
                ? createdAt.toLocaleString(currentLang === 'ru' ? 'ru-RU' : 'ro-RO', {
                    day: '2-digit',
                    month: '2-digit',
                    year: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                })
                : '';

            const wrapper = document.createElement('div');
            wrapper.className = `notification-item${item.readAt ? '' : ' unread'}`;
            wrapper.dataset.notificationId = item.id;
            wrapper.innerHTML = `
                <div class="notification-item-header">
                    <span class="notification-item-title">${item.title}</span>
                    <span class="notification-item-date">${dateLabel}</span>
                </div>
                <div class="notification-item-message">${item.message}</div>
            `;
            wrapper.addEventListener('click', () => {
                if (!item.readAt) {
                    fetch(`/api/notifications/${item.id}/read`, { method: 'POST' })
                        .then(() => {
                            wrapper.classList.remove('unread');
                            refreshNotifications();
                        })
                        .catch(() => {});
                }
            });
            list.appendChild(wrapper);
        });
    };

    const refreshNotifications = () => {
        fetch(`/api/notifications/unread-count`)
            .then(response => response.ok ? response.json() : null)
            .then(data => {
                if (data && typeof data.count === 'number') {
                    updateBadge(data.count);
                } else {
                    updateBadge(0);
                }
            })
            .catch(() => {
                updateBadge(0);
            });

        fetch(`/api/notifications?limit=10&lang=${currentLang}`)
            .then(response => response.ok ? response.json() : [])
            .then(renderNotifications)
            .catch(() => {
                renderNotifications([]);
            });
    };

    bell.addEventListener('click', (event) => {
        event.stopPropagation();
        dropdown.classList.toggle('open');

        const hamburger = document.querySelector('.hamburger');
        const navMenu = document.querySelector('.nav-menu');
        const navAuth = document.querySelector('.nav-auth');
        if (hamburger && hamburger.classList.contains('active')) {
            navMenu?.classList.add('active');
            navAuth?.classList.add('active');
        }
    });

    if (wrapper) {
        const label = wrapper.querySelector('.notification-label');
        if (label) {
            label.addEventListener('click', (event) => {
                event.stopPropagation();
                dropdown.classList.toggle('open');
            });
        }
    }

    dropdown.addEventListener('click', (event) => {
        event.stopPropagation();
    });

    document.addEventListener('click', (event) => {
        if (!event.target.closest('.notification-wrapper')) {
            dropdown.classList.remove('open');
        }
    });

    if (markAll) {
        markAll.addEventListener('click', (event) => {
            event.stopPropagation();
            fetch('/api/notifications/read-all', { method: 'POST' })
                .then(() => refreshNotifications())
                .catch(() => {});
        });
    }

    refreshNotifications();
    initializePushNotifications({
        currentLang,
        pushPrompt,
        pushButton,
        pushStatus
    });
}

let pushRegistrationPromise = null;
let pushPublicKeyPromise = null;

function initializePushNotifications(context) {
    const { currentLang, pushPrompt, pushButton, pushStatus } = context;

    if (!pushPrompt || !pushButton || !pushStatus) {
        return;
    }

    if (!('serviceWorker' in navigator) || !('PushManager' in window) || !('Notification' in window)) {
        pushPrompt.hidden = true;
        pushStatus.hidden = true;
        return;
    }

    const pushTexts = getNotificationTexts();
    const showStatus = (text) => {
        pushStatus.textContent = text;
        pushStatus.hidden = false;
    };

    const hideStatus = () => {
        pushStatus.hidden = true;
    };

    const pushText = pushPrompt.querySelector('.notification-push-text');
    const enableText = pushButton.getAttribute('data-enable-text') || pushTexts.pushEnable;
    const disableText = pushButton.getAttribute('data-disable-text') || pushTexts.pushDisable;

    const setInactiveUi = () => {
        if (pushText) {
            pushText.textContent = pushTexts.pushPrompt;
        }
        pushButton.textContent = enableText;
        pushButton.classList.remove('is-danger');
        pushButton.dataset.mode = 'enable';
        pushPrompt.hidden = false;
    };

    const setActiveUi = () => {
        if (pushText) {
            pushText.textContent = pushTexts.pushEnabled;
        }
        pushButton.textContent = disableText;
        pushButton.classList.add('is-danger');
        pushButton.dataset.mode = 'disable';
        pushPrompt.hidden = false;
    };

    const handlePermissionState = () => {
        if (Notification.permission === 'denied') {
            pushPrompt.hidden = true;
            showStatus(pushTexts.pushBlocked);
            return false;
        }

        if (Notification.permission === 'granted') {
            hideStatus();
            getPushRegistration()
                .then((registration) => registration.pushManager.getSubscription())
                .then((subscription) => {
                    if (subscription) {
                        setActiveUi();
                        showStatus(pushTexts.pushEnabled);
                    } else {
                        setInactiveUi();
                    }
                })
                .catch(() => {});
            return false;
        }

        setInactiveUi();
        hideStatus();
        return true;
    };

    handlePermissionState();

    pushButton.addEventListener('click', async (event) => {
        event.preventDefault();
        event.stopPropagation();

        const registration = await getPushRegistration();
        const subscription = await registration.pushManager.getSubscription();

        if (subscription && (pushButton.dataset.mode === 'disable' || Notification.permission === 'granted')) {
            const endpoint = subscription.endpoint;
            await subscription.unsubscribe();
            fetch('/api/push/unsubscribe', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ endpoint })
            }).catch(() => {});
            setInactiveUi();
            hideStatus();
            return;
        }

        const permission = await Notification.requestPermission();
        if (permission !== 'granted') {
            handlePermissionState();
            return;
        }

        ensurePushSubscription(currentLang)
            .then((subscribed) => {
                if (subscribed) {
                    setActiveUi();
                    showStatus(pushTexts.pushEnabled);
                }
            })
            .catch(() => {});
    });
}

async function ensurePushSubscription(currentLang) {
    const registration = await getPushRegistration();
    const existingSubscription = await registration.pushManager.getSubscription();
    const subscription = existingSubscription || await subscribeForPush(registration);

    if (!subscription) {
        return false;
    }

    const payload = subscription.toJSON();
    payload.language = currentLang;
    payload.userAgent = navigator.userAgent;

    await fetch('/api/push/subscribe', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    }).catch(() => {});
    return true;
}

async function subscribeForPush(registration) {
    const vapidKey = await getPushPublicKey();
    if (!vapidKey) {
        return null;
    }

    return registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: vapidKey
    });
}

function getPushRegistration() {
    if (!pushRegistrationPromise) {
        pushRegistrationPromise = navigator.serviceWorker.register('/sw.js');
    }
    return pushRegistrationPromise;
}

async function getPushPublicKey() {
    if (!pushPublicKeyPromise) {
        pushPublicKeyPromise = fetch('/api/push/vapid-public-key')
            .then(response => response.ok ? response.text() : '')
            .then(key => key ? urlBase64ToUint8Array(key) : null)
            .catch(() => null);
    }
    return pushPublicKeyPromise;
}

function urlBase64ToUint8Array(base64String) {
    const padding = '='.repeat((4 - (base64String.length % 4)) % 4);
    const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);
    for (let i = 0; i < rawData.length; ++i) {
        outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray;
}

/**
 * Salvează URL-ul curent și redirecționează către pagina de login
 * @param {Event} event - Event-ul de click
 * @param {string} loginUrl - URL-ul paginii de login
 */
function saveCurrentUrlAndRedirect(event, loginUrl) {
    event.preventDefault();
    
    const currentUrl = window.location.pathname + window.location.search;
    const isLoginPage = currentUrl.includes('/login');
    const isRegisterPage = currentUrl.includes('/register');
    const isLogoutPage = currentUrl.includes('/logout');
    
    // Nu salva URL-ul dacă suntem pe paginile de autentificare
    if (!isLoginPage && !isRegisterPage && !isLogoutPage) {
        sessionStorage.setItem('redirectAfterLogin', currentUrl);
        console.log('🔗 Saved current URL for redirect:', currentUrl);
    }
    
    // Redirecționează către pagina de login
    window.location.href = loginUrl;
}

function logout() {
    console.log('Logging out...');
    fetch('/api/auth/logout', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => {
        console.log('Logout response status:', response.status);
        return response.json();
    })
    .then(data => {
        console.log('Logout response data:', data);
        if (data.success) {
            console.log('Logout successful');
            updateNavbarForLoggedOutUser();
            // If we're on the profile page, redirect to home
            if (window.location.pathname === '/profile') {
                window.location.href = '/';
            }
        } else {
            console.error('Logout failed:', data.message);
        }
    })
    .catch(error => {
        console.error('Error during logout:', error);
        // Even if logout fails, update navbar to logged out state
        updateNavbarForLoggedOutUser();
    });
}
