// Edit Ride Translations
const editRideTranslations = {
    'ro': {
        'title': 'Editează cursă',
        'subtitle': 'Modifică detaliile cursei tale',
        'fromLocation': 'De la',
        'fromLocationPlaceholder': 'Ex: Chișinău',
        'toLocation': 'Până la',
        'toLocationPlaceholder': 'Ex: București',
        'travelDate': 'Data călătoriei',
        'departureTime': 'Ora plecării',
        'availableSeats': 'Locuri disponibile',
        'description': 'Descriere (opțional)',
        'descriptionPlaceholder': 'Detalii suplimentare despre cursă...',
        'packageOnly': 'Doar transport colete',
        'transportAndPackages': 'Transport și colete',
        'updateButton': 'Actualizează cursă',
        'cancelButton': 'Anulează',
        'loadingError': 'ID-ul cursei nu a fost găsit în URL',
        'loadError': 'Eroare la încărcarea datelor cursei',
        'updateSuccess': 'Cursa a fost actualizată cu succes!',
        'updateError': 'Eroare la actualizarea cursei',
        'permissionError': 'Nu aveți permisiunea de a edita această cursă',
        'validationErrors': {
            'locationsRequired': 'Locațiile de plecare și destinație sunt obligatorii',
            'dateRequired': 'Data călătoriei este obligatorie',
            'timeRequired': 'Ora plecării este obligatorie',
            'seatsRequired': 'Numărul de locuri disponibile trebuie să fie între 1 și 10',
            'pastDate': 'Data călătoriei nu poate fi în trecut'
        }
    },
    'ru': {
        'title': 'Редактировать поездку',
        'subtitle': 'Измените детали вашей поездки',
        'fromLocation': 'Откуда',
        'fromLocationPlaceholder': 'Например: Кишинёв',
        'toLocation': 'Куда',
        'toLocationPlaceholder': 'Например: Бухарест',
        'travelDate': 'Дата поездки',
        'departureTime': 'Время отправления',
        'availableSeats': 'Доступные места',
        'description': 'Описание (необязательно)',
        'descriptionPlaceholder': 'Дополнительные детали о поездке...',
        'packageOnly': 'Только перевозка грузов',
        'transportAndPackages': 'Перевозка пассажиров и грузов',
        'updateButton': 'Обновить поездку',
        'cancelButton': 'Отмена',
        'loadingError': 'ID поездки не найден в URL',
        'loadError': 'Ошибка при загрузке данных поездки',
        'updateSuccess': 'Поездка успешно обновлена!',
        'updateError': 'Ошибка при обновлении поездки',
        'permissionError': 'У вас нет прав для редактирования этой поездки',
        'validationErrors': {
            'locationsRequired': 'Места отправления и назначения обязательны',
            'dateRequired': 'Дата поездки обязательна',
            'timeRequired': 'Время отправления обязательно',
            'seatsRequired': 'Количество доступных мест должно быть от 1 до 10',
            'pastDate': 'Дата поездки не может быть в прошлом'
        }
    }
};

// Function to get translation
function getEditRideTranslation(key, language = 'ro') {
    const lang = language === 'ru' ? 'ru' : 'ro';
    const keys = key.split('.');
    let translation = editRideTranslations[lang];
    
    for (const k of keys) {
        if (translation && translation[k]) {
            translation = translation[k];
        } else {
            return key; // Return key if translation not found
        }
    }
    
    return translation;
}

// Function to update page translations
function updateEditRideTranslations(language = 'ro') {
    const lang = language === 'ru' ? 'ru' : 'ro';
    
    // Update title
    const titleElement = document.querySelector('h1 span');
    if (titleElement) {
        titleElement.textContent = editRideTranslations[lang].title;
    }
    
    // Update subtitle
    const subtitleElement = document.querySelector('.form-header p');
    if (subtitleElement) {
        subtitleElement.textContent = editRideTranslations[lang].subtitle;
    }
    
    // Update form labels and placeholders
    const labels = {
        'fromLocation': editRideTranslations[lang].fromLocation,
        'toLocation': editRideTranslations[lang].toLocation,
        'travelDate': editRideTranslations[lang].travelDate,
        'departureTime': editRideTranslations[lang].departureTime,
        'availableSeats': editRideTranslations[lang].availableSeats,
        'description': editRideTranslations[lang].description
    };
    
    const placeholders = {
        'fromLocation': editRideTranslations[lang].fromLocationPlaceholder,
        'toLocation': editRideTranslations[lang].toLocationPlaceholder,
        'description': editRideTranslations[lang].descriptionPlaceholder
    };
    
    // Update labels
    Object.keys(labels).forEach(id => {
        const label = document.querySelector(`label[for="${id}"] span`);
        if (label) {
            label.textContent = labels[id];
        }
    });
    
    // Update placeholders
    Object.keys(placeholders).forEach(id => {
        const input = document.getElementById(id);
        if (input) {
            input.placeholder = placeholders[id];
        }
    });
    
    // Update checkbox labels
    const packageOnlyLabel = document.querySelector('label[for="isPackageOnly"] span:last-child');
    if (packageOnlyLabel) {
        packageOnlyLabel.textContent = editRideTranslations[lang].packageOnly;
    }
    
    const transportLabel = document.querySelector('label[for="transportAndPackages"] span:last-child');
    if (transportLabel) {
        transportLabel.textContent = editRideTranslations[lang].transportAndPackages;
    }
    
    // Update buttons
    const updateButton = document.querySelector('button[type="submit"] span');
    if (updateButton) {
        updateButton.textContent = editRideTranslations[lang].updateButton;
    }
    
    const cancelButton = document.querySelector('a[href*="profile"] span');
    if (cancelButton) {
        cancelButton.textContent = editRideTranslations[lang].cancelButton;
    }
}
