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
        });
        
        // Închide meniul când se face click în afara lui
        document.addEventListener('click', function(e) {
            const isClickInsideNavbar = e.target.closest('.navbar');
            const isClickOnHamburger = e.target.closest('.hamburger');
            
            if (!isClickInsideNavbar && !isClickOnHamburger) {
                // Click în afara navbar-ului, închide meniul
                newHamburger.classList.remove('active');
                navMenu.classList.remove('active');
                navAuth.classList.remove('active');
            }
        });
        
        // Închide meniul când se face click pe un link din meniu
        const navLinks = document.querySelectorAll('.nav-menu a, .nav-auth a, .nav-auth button');
        navLinks.forEach(link => {
            link.addEventListener('click', function() {
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
                        <i class="fas fa-user-circle"></i>
                        <span class="user-name">${user.firstName}</span>
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
