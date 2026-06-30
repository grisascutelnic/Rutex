/**
 * Locality Autocomplete Component
 * Suportă căutarea localităților din Moldova cu cache hibrid (local + Google Places API)
 * Suportă limbi: română (ro) și rusă (ru)
 */
class LocalityAutocomplete {
    constructor(options = {}) {
        this.options = {
            inputSelector: options.inputSelector || '.locality-input',
            resultsContainerSelector: options.resultsContainerSelector || '.locality-results',
            apiBaseUrl: options.apiBaseUrl || '/api/localities',
            language: options.language || 'ro',
            limit: options.limit || 10,
            minQueryLength: options.minQueryLength || 2,
            debounceDelay: options.debounceDelay || 300,
            includeDistrict: options.includeDistrict !== false, // Default to true
            ...options
        };
        
        this.input = document.querySelector(this.options.inputSelector);
        this.resultsContainer = document.querySelector(this.options.resultsContainerSelector);
        this.debounceTimer = null;
        this.currentRequest = null;
        this.selectedIndex = -1;
        this.results = [];
        this.suppressNextInput = false;
        
        if (!this.input) {
            console.error('LocalityAutocomplete: Input element not found');
            return;
        }
        
        console.log('LocalityAutocomplete initialized for input:', this.input.id);
        console.log('Results container selector:', this.options.resultsContainerSelector);
        console.log('Results container found:', this.resultsContainer);
        
        // Register this instance globally
        if (!window.localityAutocompleteInstances) {
            window.localityAutocompleteInstances = [];
        }
        window.localityAutocompleteInstances.push(this);
        
        this.init();
    }
    
    init() {
        this.setupEventListeners();
        this.createResultsContainer();
        this.setupKeyboardNavigation();
    }
    
    setupEventListeners() {
        // Input events
        this.input.addEventListener('input', (e) => this.handleInput(e));
        this.input.addEventListener('focus', () => this.showResults());
        this.input.addEventListener('blur', () => this.hideResultsDelayed());
        
        // Click events
        document.addEventListener('click', (e) => this.handleDocumentClick(e));
        
        // Event delegation for result clicks - REMOVED GLOBAL LISTENER
        // We'll handle clicks directly on result items
        
        // Language change events (if language selector exists)
        const languageSelectors = document.querySelectorAll('.language-selector, [data-language]');
        languageSelectors.forEach(selector => {
            selector.addEventListener('change', (e) => this.handleLanguageChange(e));
        });
    }
    
    createResultsContainer() {
        if (!this.resultsContainer) {
            this.resultsContainer = document.createElement('div');
            this.resultsContainer.className = 'locality-results';
            
            // Create unique ID for this results container
            const inputId = this.input.id;
            if (inputId) {
                this.resultsContainer.id = `${inputId}-suggestions`;
            } else {
                this.resultsContainer.id = `locality-results-${Math.random().toString(36).substring(2, 9)}`;
            }
            
            this.resultsContainer.style.cssText = `
                position: absolute;
                top: 100%;
                left: 0;
                right: 0;
                background: white;
                border: 1px solid #ddd;
                border-top: none;
                border-radius: 0 0 4px 4px;
                box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                max-height: 300px;
                overflow-y: auto;
                z-index: 1000;
                display: none;
            `;
            
            // Ensure parent has relative positioning
            if (this.input.parentNode) {
                this.input.parentNode.style.position = 'relative';
                this.input.parentNode.appendChild(this.resultsContainer);
            } else {
                this.input.after(this.resultsContainer);
            }
        } else {
            // If results container already exists, ensure it has the right styling
            this.resultsContainer.className = 'locality-results';
            this.resultsContainer.style.cssText = `
                position: absolute;
                top: 100%;
                left: 0;
                right: 0;
                background: white;
                border: 1px solid #ddd;
                border-top: none;
                border-radius: 0 0 4px 4px;
                box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                max-height: 300px;
                overflow-y: auto;
                z-index: 1000;
                display: none;
            `;
            
            // Ensure parent has relative positioning
            if (this.input.parentNode) {
                this.input.parentNode.style.position = 'relative';
            }
        }
    }
    
    setupKeyboardNavigation() {
        this.input.addEventListener('keydown', (e) => {
            switch (e.key) {
                case 'ArrowDown':
                    e.preventDefault();
                    this.navigateResults(1);
                    break;
                case 'ArrowUp':
                    e.preventDefault();
                    this.navigateResults(-1);
                    break;
                case 'Enter':
                    e.preventDefault();
                    this.selectCurrentResult();
                    break;
                case 'Escape':
                    this.hideResults();
                    break;
            }
        });
    }
    
    handleInput(e) {
        if (this.suppressNextInput) {
            this.suppressNextInput = false;
            this.hideResults();
            return;
        }

        const query = e.target.value.trim();
        
        // Clear previous timer
        if (this.debounceTimer) {
            clearTimeout(this.debounceTimer);
        }
        
        // Cancel previous request
        if (this.currentRequest) {
            this.currentRequest.abort();
        }
        
        // Check minimum query length
        if (query.length < this.options.minQueryLength) {
            this.hideResults();
            return;
        }
        
        // Debounce the search
        this.debounceTimer = setTimeout(() => {
            this.searchLocalities(query);
        }, this.options.debounceDelay);
    }
    
    async searchLocalities(query) {
        // Don't search if input has a complete value (contains comma, indicating a selected locality)
        if (this.input && this.input.value.includes(',')) {
            this.hideResults();
            return;
        }
        
        try {
            const url = new URL(`${this.options.apiBaseUrl}/autocomplete`, window.location.origin);
            url.searchParams.set('query', query);
            url.searchParams.set('language', this.options.language);
            url.searchParams.set('limit', this.options.limit);
            
            console.log('Making autocomplete request to:', url.toString());
            
            this.currentRequest = new AbortController();
            const response = await fetch(url, {
                signal: this.currentRequest.signal
            });
            
            console.log('Response status:', response.status);
            console.log('Response headers:', response.headers);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const responseText = await response.text();
            console.log('Raw response:', responseText);
            
            this.results = JSON.parse(responseText);
            console.log('Parsed results:', this.results);
            
            this.displayResults();
            
        } catch (error) {
            if (error.name === 'AbortError') {
                // Request was cancelled, ignore
                return;
            }
            console.error('Error searching localities:', error);
            this.showError('Eroare la căutarea localităților');
        }
    }
    
    displayResults() {
        if (!this.resultsContainer) return;
        
        if (this.results.length === 0) {
            this.resultsContainer.innerHTML = `
                <div class="locality-result-item no-results">
                    <span>Nu s-au găsit localități</span>
                </div>
            `;
        } else {
            this.resultsContainer.innerHTML = this.results
                .map((locality, index) => this.createResultItem(locality, index))
                .join('');
            
            // Add click listeners directly to result items
            const resultItems = this.resultsContainer.querySelectorAll('.locality-result-item:not(.no-results):not(.error)');
            resultItems.forEach((item, index) => {
                item.addEventListener('click', (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    console.log('Direct click on result item:', index, this.results[index]);
                    this.selectLocality(this.results[index]);
                });
            });
        }
        
        this.showResults();
        this.selectedIndex = -1;
    }
    
    createResultItem(locality, index) {
        const name = this.options.language === 'ru' ? locality.nameRu : locality.nameRo;
        const districtName = this.options.language === 'ru' ? locality.districtNameRu : locality.districtNameRo;
        const countryName = this.options.language === 'ru' ? locality.countryNameRu : locality.countryNameRo;
        
        // Afișează numele cu raionul doar dacă nu conține deja "Raionul" sau "Район"
        let displayName = name;
        if (districtName && !name.includes('Raionul') && !name.includes('Район')) {
            displayName = `${name}, ${districtName}`;
        }
        
        return `
            <div class="locality-result-item" data-index="${index}" data-locality-id="${locality.id}" style="cursor: pointer;">
                <div class="locality-name">${this.highlightQuery(displayName)}</div>
                <div class="locality-details">
                    ${countryName ? `<span class="locality-country">• ${countryName}</span>` : ''}
                </div>
            </div>
        `;
    }
    
    highlightQuery(text) {
        const query = this.input.value.trim();
        if (!query) return text;
        
        const regex = new RegExp(`(${this.escapeRegex(query)})`, 'gi');
        return text.replace(regex, '<mark>$1</mark>');
    }
    
    escapeRegex(string) {
        return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }
    
    getLocalityTypeLabel(type, language) {
        const labels = {
            ro: {
                'city': 'Oraș',
                'town': 'Oraș',
                'village': 'Sat',
                'municipality': 'Municipiu',
                'suburb': 'Cartier',
                'neighborhood': 'Cartier'
            },
            ru: {
                'city': 'Город',
                'town': 'Город',
                'village': 'Село',
                'municipality': 'Муниципий',
                'suburb': 'Район',
                'neighborhood': 'Район'
            }
        };
        
        return labels[language]?.[type] || type;
    }
    
    showResults() {
        if (this.resultsContainer) {
            this.resultsContainer.style.display = 'block';
        }
    }
    
    hideResults() {
        if (this.resultsContainer) {
            this.resultsContainer.style.display = 'none';
        }
    }
    
    hideResultsDelayed() {
        // Use a longer delay to allow for clicks on results
        setTimeout(() => this.hideResults(), 300);
    }
    
    navigateResults(direction) {
        if (this.results.length === 0) return;
        
        this.selectedIndex += direction;
        
        if (this.selectedIndex >= this.results.length) {
            this.selectedIndex = 0;
        } else if (this.selectedIndex < 0) {
            this.selectedIndex = this.results.length - 1;
        }
        
        this.updateSelection();
    }
    
    updateSelection() {
        const items = this.resultsContainer.querySelectorAll('.locality-result-item');
        
        items.forEach((item, index) => {
            item.classList.toggle('selected', index === this.selectedIndex);
        });
    }
    
    selectCurrentResult() {
        if (this.selectedIndex >= 0 && this.selectedIndex < this.results.length) {
            const locality = this.results[this.selectedIndex];
            this.selectLocality(locality);
        }
    }
    
    selectLocality(locality) {
        console.log('selectLocality called with:', locality);
        console.log('Input element:', this.input);
        console.log('Input ID:', this.input.id);
        
        let displayName = this.options.language === 'ru' ? locality.nameRu : locality.nameRo;
        const districtName = this.options.language === 'ru' ? locality.districtNameRu : locality.districtNameRo;
        
        // Include district name only if the locality name doesn't already contain "Raionul" or "Район"
        if (districtName && !displayName.includes('Raionul') && !displayName.includes('Район')) {
            displayName = `${displayName}, ${districtName}`;
        }
        
        console.log('Setting input value to:', displayName);
        
        // Set the input value
        if (this.input) {
            this.suppressNextInput = true;
            this.input.value = displayName;
            
            // Trigger input event to notify any listeners
            this.input.dispatchEvent(new Event('input', { bubbles: true }));
            this.input.dispatchEvent(new Event('change', { bubbles: true }));
        }
        
        // Trigger custom event
        const event = new CustomEvent('localitySelected', {
            detail: { locality, input: this.input }
        });
        document.dispatchEvent(event);
        console.log('localitySelected event dispatched');
        
        if (this.debounceTimer) {
            clearTimeout(this.debounceTimer);
            this.debounceTimer = null;
        }
        if (this.currentRequest) {
            this.currentRequest.abort();
            this.currentRequest = null;
        }

        // Hide results immediately and prevent re-showing
        this.hideResults();
        this.results = [];
        this.selectedIndex = -1;
        if (this.resultsContainer) {
            this.resultsContainer.innerHTML = '';
        }
        
        // Increment search count
        this.incrementSearchCount(locality.id);
    }
    
    async incrementSearchCount(localityId) {
        try {
            // For now, we'll use the local search endpoint since most results come from local database
            // In a more sophisticated implementation, we could track the source of each result
            await fetch(`${this.options.apiBaseUrl}/${localityId}/increment-search`, {
                method: 'POST'
            });
        } catch (error) {
            console.error('Error incrementing search count:', error);
        }
    }
    
    handleDocumentClick(e) {
        // Don't hide results if clicking on the input or results container
        if (e.target === this.input || this.resultsContainer?.contains(e.target)) {
            return;
        }
        
        // Hide results if clicking elsewhere
        this.hideResults();
    }
    
    handleResultClick(e) {
        // Check if click is on a result item
        const resultItem = e.target.closest('.locality-result-item');
        if (!resultItem) {
            return;
        }
        
        // Don't handle clicks on error or no-results items
        if (resultItem.classList.contains('error') || resultItem.classList.contains('no-results')) {
            return;
        }
        
        // Find which autocomplete instance this result belongs to
        const resultsContainer = resultItem.closest('.locality-results');
        if (!resultsContainer) {
            console.error('Could not find results container');
            return;
        }
        
        // Find the input associated with this results container using the unique ID
        const inputId = resultsContainer.id.replace('-suggestions', '');
        const input = document.getElementById(inputId);
        if (!input) {
            console.error('Could not find input associated with results container:', resultsContainer.id);
            return;
        }
        
        // Find the autocomplete instance for this input
        const autocompleteInstance = this.findAutocompleteInstance(input);
        if (!autocompleteInstance) {
            console.error('Could not find autocomplete instance for input:', input.id);
            return;
        }
        
        const index = parseInt(resultItem.dataset.index);
        if (isNaN(index) || index < 0 || index >= autocompleteInstance.results.length) {
            console.error('Invalid result index:', index);
            return;
        }
        
        console.log('Result item clicked via delegation:', index, autocompleteInstance.results[index]);
        
        // Prevent default and stop propagation BEFORE calling selectLocality
        e.preventDefault();
        e.stopPropagation();
        
        // Call selectLocality on the correct instance
        autocompleteInstance.selectLocality(autocompleteInstance.results[index]);
        
        // Force focus back to the input
        setTimeout(() => {
            input.focus();
        }, 100);
    }
    
    findAutocompleteInstance(input) {
        // Find the autocomplete instance that owns this input
        for (const instance of window.localityAutocompleteInstances || []) {
            if (instance.input === input) {
                return instance;
            }
        }
        return null;
    }
    
    handleLanguageChange(e) {
        const newLanguage = e.target.value || e.target.dataset.language;
        if (newLanguage && newLanguage !== this.options.language) {
            this.options.language = newLanguage;
            
            // Re-search if there's a current query
            const query = this.input.value.trim();
            if (query.length >= this.options.minQueryLength) {
                this.searchLocalities(query);
            }
        }
    }
    
    showError(message) {
        if (this.resultsContainer) {
            this.resultsContainer.innerHTML = `
                <div class="locality-result-item error">
                    <span>${message}</span>
                </div>
            `;
            this.showResults();
        }
    }
    
    // Public methods
    setLanguage(language) {
        this.options.language = language;
    }
    
    getSelectedLocality() {
        return this.results[this.selectedIndex] || null;
    }
    
    destroy() {
        if (this.debounceTimer) {
            clearTimeout(this.debounceTimer);
        }
        if (this.currentRequest) {
            this.currentRequest.abort();
        }
        this.hideResults();
        
        // Remove from global instances
        if (window.localityAutocompleteInstances) {
            const index = window.localityAutocompleteInstances.indexOf(this);
            if (index > -1) {
                window.localityAutocompleteInstances.splice(index, 1);
            }
        }
    }
}

// CSS Styles for the autocomplete component
const localityAutocompleteStyles = `
<style>
.locality-results {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    font-size: 14px;
}

.locality-result-item {
    padding: 12px 16px;
    cursor: pointer;
    border-bottom: 1px solid #f0f0f0;
    transition: background-color 0.2s ease;
}

.locality-result-item:hover,
.locality-result-item.selected {
    background-color: #f8f9fa;
}

.locality-result-item:last-child {
    border-bottom: none;
}

.locality-result-item.no-results,
.locality-result-item.error {
    color: #6c757d;
    cursor: default;
}

.locality-result-item.error {
    color: #dc3545;
}

.locality-name {
    font-weight: 500;
    color: #212529;
    margin-bottom: 4px;
}

.locality-name mark {
    background-color: #fff3cd;
    color: #856404;
    padding: 0 2px;
    border-radius: 2px;
}

.locality-details {
    font-size: 12px;
    color: #6c757d;
}

.locality-type {
    font-weight: 500;
}

.locality-district {
    margin-left: 8px;
}

.locality-country {
    margin-left: 8px;
}

/* Loading state */
.locality-results.loading::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 20px;
    height: 20px;
    margin: -10px 0 0 -10px;
    border: 2px solid #f3f3f3;
    border-top: 2px solid #007bff;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}
</style>
`;

// Inject styles
document.head.insertAdjacentHTML('beforeend', localityAutocompleteStyles);

// Export for use in other modules
window.LocalityAutocomplete = LocalityAutocomplete;
