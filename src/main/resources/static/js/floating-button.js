// Floating Action Button Management
class FloatingActionButton {
    constructor() {
        this.button = null;
        this.currentPage = this.getCurrentPage();
        this.init();
    }

    // Detectează pagina curentă
    getCurrentPage() {
        const path = window.location.pathname;
        const segments = path.split('/').filter(segment => segment);
        
        // Paginile unde nu vrem să afișăm butonul
        const excludedPages = ['login', 'register', 'users', 'forgot-password', 'reset-password', 'edit-profile', 'edit-ride'];
        
        // Verificăm dacă suntem pe o pagină exclusă
        for (const segment of segments) {
            if (excludedPages.includes(segment)) {
                return 'excluded';
            }
        }
        
        // Determinăm tipul de pagină
        if (segments.includes('add-ride')) {
            return 'add-ride';
        } else if (segments.includes('edit-ride')) {
            return 'edit-ride';
        } else if (segments.includes('profile')) {
            return 'profile';
        } else if (segments.includes('rides')) {
            return 'rides';
        } else if (segments.includes('ride')) {
            return 'ride-details';
        } else if (segments.length === 0 || segments.includes('index') || segments.includes('home')) {
            return 'home';
        } else {
            return 'other';
        }
    }

    // Inițializează butonul
    init() {
        // Nu afișăm butonul pe paginile excluse
        if (this.currentPage === 'excluded') {
            return;
        }

        this.createButton();
        this.addEventListeners();
        this.showButton();
    }

    // Creează elementul butonului
    createButton() {
        this.button = document.createElement('button');
        this.button.className = 'floating-action-button';
        this.button.id = 'floating-action-btn';
        
        // Iconița
        const icon = document.createElement('i');
        icon.className = 'fas fa-plus';
        this.button.appendChild(icon);
        
        // Textul în funcție de limbă
        const text = document.createElement('span');
        text.className = 'floating-button-text';
        text.textContent = this.getButtonText();
        this.button.appendChild(text);
        
        // Setăm aria-label și title
        this.button.setAttribute('aria-label', this.getButtonText());
        this.button.setAttribute('title', this.getButtonText());
        
        document.body.appendChild(this.button);
    }

    // Adaugă event listeners
    addEventListeners() {
        if (!this.button) return;

        this.button.addEventListener('click', (e) => {
            e.preventDefault();
            this.handleClick();
        });

        // Adăugăm efecte de hover și focus pentru accesibilitate
        this.button.addEventListener('mouseenter', () => {
            this.button.style.transform = 'translateY(-3px) scale(1.05)';
        });

        this.button.addEventListener('mouseleave', () => {
            this.button.style.transform = 'translateY(0) scale(1)';
        });

        // Keyboard navigation
        this.button.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                this.handleClick();
            }
        });
    }

    // Gestionează click-ul pe buton
    handleClick() {
        console.log('Floating action button clicked');
        
        // Dacă suntem deja pe pagina add-ride, scrollăm la formular
        if (this.currentPage === 'add-ride') {
            const form = document.getElementById('add-ride-form');
            if (form) {
                form.scrollIntoView({ behavior: 'smooth', block: 'start' });
                return;
            }
        }

        // Altfel, redirecționăm către add-ride
        this.redirectToAddRide();
    }

    // Redirecționează către pagina add-ride
    redirectToAddRide() {
        const currentLang = this.getCurrentLanguage();
        const targetUrl = `/${currentLang}/add-ride`;
        
        // Verificăm dacă utilizatorul este logat
        const isLoggedIn = document.querySelector('.nav-user') !== null;
        
        if (isLoggedIn) {
            // Dacă este logat, mergem direct la add-ride
            console.log('User is logged in, redirecting to:', targetUrl);
            window.location.href = targetUrl;
        } else {
            // Dacă nu este logat, salvăm destinația și mergem la login
            console.log('User is not logged in, saving destination and redirecting to login');
            sessionStorage.setItem('redirectAfterLogin', targetUrl);
            sessionStorage.setItem('floatingButtonRedirect', 'true');
            
            // Redirecționăm la pagina de login
            const loginUrl = `/${currentLang}/login`;
            window.location.href = loginUrl;
        }
    }

    // Detectează limba curentă
    getCurrentLanguage() {
        const currentLangElement = document.querySelector('.current-lang');
        if (currentLangElement) {
            return currentLangElement.textContent === 'RO' ? 'ro' : 'ru';
        }
        
        // Fallback: verificăm URL-ul
        const path = window.location.pathname;
        const segments = path.split('/').filter(segment => segment);
        if (segments.length > 0 && (segments[0] === 'ro' || segments[0] === 'ru')) {
            return segments[0];
        }
        
        return 'ro'; // Default
    }

    // Obține textul butonului în funcție de limbă
    getButtonText() {
        const language = this.getCurrentLanguage();
        const texts = {
            'ro': 'Adaugă o cursă',
            'ru': 'Добавить поездку'
        };
        return texts[language] || texts['ro'];
    }

    // Afișează butonul cu animație
    showButton() {
        if (!this.button) return;

        // Adăugăm clasa pentru animație
        this.button.style.opacity = '0';
        this.button.style.transform = 'translateY(20px) scale(0.8)';
        
        // Animație de intrare
        setTimeout(() => {
            this.button.style.transition = 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)';
            this.button.style.opacity = '1';
            this.button.style.transform = 'translateY(0) scale(1)';
        }, 100);
    }

    // Ascunde butonul
    hideButton() {
        if (!this.button) return;
        
        this.button.style.opacity = '0';
        this.button.style.transform = 'translateY(20px) scale(0.8)';
        
        setTimeout(() => {
            if (this.button && this.button.parentNode) {
                this.button.parentNode.removeChild(this.button);
            }
        }, 400);
    }

    // Actualizează butonul pentru o nouă pagină
    updateForPage(newPage) {
        this.currentPage = newPage;
        
        if (newPage === 'excluded') {
            this.hideButton();
        } else if (!this.button) {
            this.init();
        }
    }

    // Actualizează textul butonului când se schimbă limba
    updateButtonText() {
        if (this.button) {
            const textElement = this.button.querySelector('.floating-button-text');
            if (textElement) {
                textElement.textContent = this.getButtonText();
            }
            this.button.setAttribute('aria-label', this.getButtonText());
            this.button.setAttribute('title', this.getButtonText());
        }
    }
}

// Inițializare când se încarcă pagina
document.addEventListener('DOMContentLoaded', function() {
    // Creăm instanța butonului fluturător
    window.floatingActionButton = new FloatingActionButton();
    
    console.log('Floating action button initialized');
    
    // Adăugăm listener pentru schimbarea limbii
    document.addEventListener('languageChanged', function(e) {
        if (window.floatingActionButton) {
            window.floatingActionButton.updateButtonText();
        }
    });
    
    // Curățăm session storage-ul pentru redirecționări dacă nu suntem pe pagina de login
    if (!window.location.pathname.includes('/login')) {
        sessionStorage.removeItem('floatingButtonRedirect');
    }
});

// Export pentru a putea fi folosit în alte fișiere
if (typeof module !== 'undefined' && module.exports) {
    module.exports = FloatingActionButton;
}
