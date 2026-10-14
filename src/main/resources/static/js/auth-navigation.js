window.authNavigation = {
    safeTarget(value) {
        if (!value || !value.startsWith('/') || value.startsWith('//') || /[\\\x00-\x1f]/.test(value)) return null;
        try {
            const target = new URL(value, window.location.origin);
            if (target.origin !== window.location.origin || /\/(login|register|logout|forgot-password|reset-password)(\/|$)/.test(target.pathname) || target.pathname.startsWith('/auth/') || target.pathname.startsWith('/oauth2/')) return null;
            return target.pathname + target.search + target.hash;
        } catch {
            return null;
        }
    },
    pendingTarget() {
        return this.safeTarget(new URLSearchParams(window.location.search).get('redirect'))
            || this.safeTarget(sessionStorage.getItem('redirectAfterLogin'));
    },
    consumeTarget() {
        const target = this.pendingTarget();
        sessionStorage.removeItem('redirectAfterLogin');
        sessionStorage.removeItem('floatingButtonRedirect');
        return target || '/' + (window.location.pathname.split('/')[1] === 'ru' ? 'ru' : 'ro');
    },
    login(target = window.location.pathname + window.location.search + window.location.hash) {
        const safeTarget = this.safeTarget(target);
        const language = window.location.pathname.split('/')[1] === 'ru' ? 'ru' : 'ro';
        if (safeTarget) sessionStorage.setItem('redirectAfterLogin', safeTarget);
        window.location.href = '/' + language + '/login' + (safeTarget ? '?redirect=' + encodeURIComponent(safeTarget) : '');
    }
};

document.addEventListener('DOMContentLoaded', () => {
    const isAuthPage = /\/(login|register|forgot-password|reset-password)$/.test(window.location.pathname);
    if (!isAuthPage && document.getElementById('logout-btn')) {
        sessionStorage.removeItem('redirectAfterLogin');
        sessionStorage.removeItem('floatingButtonRedirect');
    }
    if (isAuthPage) {
        const target = window.authNavigation.pendingTarget();
        if (target) sessionStorage.setItem('redirectAfterLogin', target);
    }
    document.addEventListener('click', event => {
        const link = event.target.closest('a');
        if (!link) return;
        const url = new URL(link.href, window.location.origin);
        if (url.origin !== window.location.origin) return;
        if (url.pathname === '/oauth2/authorization/google') {
            event.preventDefault();
            const target = window.authNavigation.pendingTarget();
            window.location.href = '/auth/google' + (target ? '?redirect=' + encodeURIComponent(target) : '');
        } else if (/^\/(ro|ru)\/login$/.test(url.pathname) && !isAuthPage) {
            event.preventDefault();
            window.authNavigation.login(url.searchParams.get('redirect') || undefined);
        }
    }, true);
});
