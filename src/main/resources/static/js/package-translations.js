// Traduceri pentru funcționalitatea de transport de colete
const packageTranslations = {
    ro: {
        'add_ride.passengers_only.title': 'Transport Pasageri',
        'add_ride.passengers_only.description': 'Creează o cursă pentru transportul de pasageri',
        'add_ride.packages_only.title': 'Transport Colete',
        'add_ride.packages_only.description': 'Transportează doar colete și pachete',
        'add_ride.passengers_and_packages.title': 'Transport Pasageri și Colete',
        'add_ride.passengers_and_packages.description': 'Creează o cursă pentru transportul de pasageri și colete',
        'add_ride.ride_type': 'Tipul de Transport',
        'add_ride.transport_and_packages': 'Transport și colete',
        'rides.package_transport': 'Tip transport',
        'rides.transport_and_packages': 'Servicii',
        'rides.per_transport': 'per transport',
        'package_only_indicator': 'Transport DOAR Colete',
        'transport_and_packages_indicator': 'Transport și colete'
    },
    ru: {
        'add_ride.passengers_only.title': 'Транспорт Пассажиров',
        'add_ride.passengers_only.description': 'Создайте поездку для перевозки пассажиров',
        'add_ride.packages_only.title': 'Транспорт Посылок',
        'add_ride.packages_only.description': 'Перевозите только посылки и пакеты',
        'add_ride.passengers_and_packages.title': 'Транспорт Пассажиров и Посылок',
        'add_ride.passengers_and_packages.description': 'Создайте поездку для перевозки пассажиров и посылок',
        'add_ride.ride_type': 'Тип Транспорта',
        'add_ride.transport_and_packages': 'Транспортирую и посылки',
        'rides.package_transport': 'Тип транспорта',
        'rides.transport_and_packages': 'Услуги',
        'rides.per_transport': 'за перевозку',
        'package_only_indicator': 'Транспортирую ТОЛЬКО Посылки',
        'transport_and_packages_indicator': 'Транспортирую и посылки'
    }
};

// Funcție pentru obținerea traducerii în funcție de limbă
function getPackageTranslation(key, language = 'ro') {
    const translations = packageTranslations[language] || packageTranslations['ro'];
    return translations[key] || key;
}

// Funcție pentru actualizarea textelor în funcție de limbă
function updatePackageTexts(language = 'ro') {
    // Actualizăm textele din opțiunile de tip transport
    const passengersOnlyTitle = document.querySelector('#ride-type-passengers-only + label .ride-type-content h4');
    const passengersOnlyDesc = document.querySelector('#ride-type-passengers-only + label .ride-type-content p');
    const packagesOnlyTitle = document.querySelector('#ride-type-packages-only + label .ride-type-content h4');
    const packagesOnlyDesc = document.querySelector('#ride-type-packages-only + label .ride-type-content p');
    const passengersAndPackagesTitle = document.querySelector('#ride-type-passengers-and-packages + label .ride-type-content h4');
    const passengersAndPackagesDesc = document.querySelector('#ride-type-passengers-and-packages + label .ride-type-content p');
    
    if (passengersOnlyTitle) {
        passengersOnlyTitle.textContent = getPackageTranslation('add_ride.passengers_only.title', language);
    }
    if (passengersOnlyDesc) {
        passengersOnlyDesc.textContent = getPackageTranslation('add_ride.passengers_only.description', language);
    }
    if (packagesOnlyTitle) {
        packagesOnlyTitle.textContent = getPackageTranslation('add_ride.packages_only.title', language);
    }
    if (packagesOnlyDesc) {
        packagesOnlyDesc.textContent = getPackageTranslation('add_ride.packages_only.description', language);
    }
    if (passengersAndPackagesTitle) {
        passengersAndPackagesTitle.textContent = getPackageTranslation('add_ride.passengers_and_packages.title', language);
    }
    if (passengersAndPackagesDesc) {
        passengersAndPackagesDesc.textContent = getPackageTranslation('add_ride.passengers_and_packages.description', language);
    }
    
    // Actualizăm indicatorii pentru transport de colete
    const packageIndicators = document.querySelectorAll('.package-only-indicator');
    packageIndicators.forEach(indicator => {
        const icon = indicator.querySelector('i');
        if (icon) {
            indicator.innerHTML = `<i class="fas fa-box"></i> ${getPackageTranslation('package_only_indicator', language)}`;
        }
    });
}

// Inițializare când se încarcă pagina
document.addEventListener('DOMContentLoaded', function() {
    // Detectăm limba curentă
    const currentLangElement = document.querySelector('.current-lang');
    let currentLanguage = 'ro';
    
    if (currentLangElement) {
        currentLanguage = currentLangElement.textContent === 'RO' ? 'ro' : 'ru';
    }
    
    // Actualizăm textele
    updatePackageTexts(currentLanguage);
    
    // Adăugăm listener pentru schimbarea limbii
    document.addEventListener('languageChanged', function(e) {
        const newLanguage = e.detail.language;
        updatePackageTexts(newLanguage);
    });
});
