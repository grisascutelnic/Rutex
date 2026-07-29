document.addEventListener('DOMContentLoaded', function() {
    const announcementTabs = document.querySelectorAll('[data-home-announcement-tab]');
    const announcementCards = document.querySelectorAll('.recent-rides [data-announcement-type]');
    const emptyState = document.getElementById('home-announcement-empty');
    const emptyCreateAction = document.getElementById('home-empty-create-action');
    const filterHomeAnnouncements = (type) => {
        announcementTabs.forEach(tab => tab.classList.toggle('active', tab.dataset.homeAnnouncementTab === type));
        let visibleCount = 0;
        announcementCards.forEach(card => {
            const visible = card.dataset.announcementType === type;
            card.hidden = !visible;
            if (visible) visibleCount++;
        });
        if (emptyState) emptyState.hidden = visibleCount > 0;
        if (emptyCreateAction) {
            const passengerRequest = type === 'PASSENGER_REQUEST';
            const language = window.location.pathname.startsWith('/ru') ? 'ru' : 'ro';
            emptyCreateAction.href = `/${language}/add-ride${passengerRequest ? '?type=passenger' : ''}`;
            const label = emptyCreateAction.querySelector('span');
            if (label) {
                label.textContent = passengerRequest
                    ? (language === 'ru' ? 'Опубликовать запрос' : 'Publică o cerere')
                    : (language === 'ru' ? 'Предложить транспорт' : 'Oferă transport');
            }
        }
    };
    announcementTabs.forEach(tab => tab.addEventListener('click', () => filterHomeAnnouncements(tab.dataset.homeAnnouncementTab)));
    if (announcementTabs.length) filterHomeAnnouncements('DRIVER_OFFER');

    // Add click event listener to the "Adaugă o cursă nouă" button
    const addRideBtn = document.getElementById('add-ride-btn');
    if (addRideBtn) {
        addRideBtn.addEventListener('click', function(e) {
            e.preventDefault();
            checkAuthAndRedirect();
        });
    }
});

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
