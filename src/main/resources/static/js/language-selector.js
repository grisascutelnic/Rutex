// Language selector functionality
(function() {
    function initLanguageSelector() {
        const langBtn = document.getElementById('lang-btn');
        const langDropdown = document.getElementById('lang-dropdown');
        const langDropdownContent = document.getElementById('lang-dropdown-content');
        const langOptions = document.querySelectorAll('.lang-option');
        
        console.log('Initializing language selector...');
        console.log('langBtn:', langBtn);
        console.log('langDropdown:', langDropdown);
        console.log('langOptions:', langOptions);
        
        if (!langBtn) {
            console.error('Language button not found!');
            return;
        }
        
        if (!langDropdown) {
            console.error('Language dropdown not found!');
            return;
        }
        
        if (langOptions.length === 0) {
            console.error('Language options not found!');
            return;
        }
        
        console.log('All elements found successfully');
        
        // Toggle dropdown
        langBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            e.preventDefault();
            console.log('Language button clicked');
            
            langDropdown.classList.toggle('show');
            const isVisible = langDropdown.classList.contains('show');
            console.log('Dropdown visibility:', isVisible);
        });
        
        // Close dropdown when clicking outside
        document.addEventListener('click', function(e) {
            if (!langBtn.contains(e.target)) {
                langDropdown.classList.remove('show');
                console.log('Dropdown closed (clicked outside)');
            }
        });
        
        // Handle language selection
        langOptions.forEach(option => {
            option.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                const selectedLang = this.getAttribute('data-lang');
                const currentLangElement = document.querySelector('.current-lang');
                const currentLang = currentLangElement ? (currentLangElement.textContent === 'RO' ? 'ro' : 'ru') : 'ro';
                
                console.log('Language selected:', selectedLang, 'Current:', currentLang);
                
                if (selectedLang !== currentLang) {
                    changeLanguage(selectedLang);
                } else {
                    console.log('Same language selected, no change needed');
                }
                
                langDropdown.classList.remove('show');
            });
        });
        
        function changeLanguage(language) {
            console.log('Changing language to:', language);
            
            // Show loading state
            const currentLangElement = document.querySelector('.current-lang');
            if (currentLangElement) {
                currentLangElement.textContent = language.toUpperCase();
            }
            
            fetch('/api/change-language', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'language=' + encodeURIComponent(language)
            })
            .then(response => {
                console.log('Response status:', response.status);
                return response.json();
            })
            .then(data => {
                console.log('Language change response:', data);
                if (data.redirectUrl) {
                    console.log('Redirecting to:', data.redirectUrl);
                    window.location.href = data.redirectUrl;
                } else {
                    console.error('No redirect URL in response');
                }
            })
            .catch(error => {
                console.error('Error changing language:', error);
                // Revert the button text if there was an error
                if (currentLangElement) {
                    currentLangElement.textContent = language === 'ro' ? 'RU' : 'RO';
                }
            });
        }
        
        console.log('Language selector initialized successfully');
    }
    
    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initLanguageSelector);
    } else {
        initLanguageSelector();
    }
})();
