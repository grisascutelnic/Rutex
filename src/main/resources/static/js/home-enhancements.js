document.documentElement.classList.add('home-effects-enabled');

document.addEventListener('DOMContentLoaded', () => {
    const homeContent = document.querySelector('.home-seo-content');
    const featuresSection = document.querySelector('.features');
    if (!homeContent || !featuresSection) {
        return;
    }

    const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const finePointer = window.matchMedia('(hover: hover) and (pointer: fine)').matches;
    const contentSections = Array.from(homeContent.querySelectorAll(':scope > section'));
    const revealSections = [featuresSection, ...contentSections];

    const cardSelectors = [
        '.feature-card',
        '.home-step',
        '.home-audience-card',
        '.home-trust-list > div',
        '.home-faq-list details'
    ];

    cardSelectors.forEach(selector => {
        document.querySelectorAll(selector).forEach((card, index) => {
            card.classList.add('home-card-reveal');
            card.style.setProperty('--reveal-order', String(index % 4));
        });
    });

    if (reducedMotion || !('IntersectionObserver' in window)) {
        revealSections.forEach(section => section.classList.add('is-visible'));
    } else {
        const revealObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (!entry.isIntersecting) {
                    return;
                }
                entry.target.classList.add('is-visible');
                observer.unobserve(entry.target);
            });
        }, {
            threshold: 0.12,
            rootMargin: '0px 0px -8%'
        });

        revealSections.forEach(section => {
            section.classList.add('home-reveal');
            revealObserver.observe(section);
        });
    }

    const progress = document.createElement('div');
    progress.className = 'home-progress';
    progress.setAttribute('aria-hidden', 'true');
    progress.innerHTML = '<span></span>';
    document.body.appendChild(progress);
    const progressValue = progress.firstElementChild;
    let scrollFrame = null;

    const updateProgress = () => {
        scrollFrame = null;
        const start = featuresSection.offsetTop;
        const end = homeContent.offsetTop + homeContent.offsetHeight - window.innerHeight;
        const value = end > start ? Math.min(1, Math.max(0, (window.scrollY - start) / (end - start))) : 0;
        progressValue.style.transform = `scaleX(${value})`;
    };

    const requestProgressUpdate = () => {
        if (scrollFrame === null) {
            scrollFrame = window.requestAnimationFrame(updateProgress);
        }
    };

    window.addEventListener('scroll', requestProgressUpdate, { passive: true });
    window.addEventListener('resize', requestProgressUpdate, { passive: true });
    updateProgress();

    const sectionNavigation = document.createElement('nav');
    sectionNavigation.className = 'home-section-nav';
    const isRussian = window.location.pathname.startsWith('/ru');
    sectionNavigation.setAttribute(
        'aria-label',
        isRussian ? 'Навигация по разделам страницы' : 'Navigare prin informațiile paginii'
    );

    const navigationButtons = contentSections.map(section => {
        const heading = section.querySelector('h2');
        const button = document.createElement('button');
        button.type = 'button';
        button.setAttribute('aria-label', heading?.textContent?.trim() || 'Secțiune');
        button.addEventListener('click', () => {
            section.scrollIntoView({ behavior: reducedMotion ? 'auto' : 'smooth', block: 'start' });
        });
        sectionNavigation.appendChild(button);
        return button;
    });

    document.body.appendChild(sectionNavigation);

    if ('IntersectionObserver' in window) {
        const activeSectionObserver = new IntersectionObserver(entries => {
            entries.forEach(entry => {
                if (!entry.isIntersecting) {
                    return;
                }
                const activeIndex = contentSections.indexOf(entry.target);
                navigationButtons.forEach((button, index) => {
                    button.classList.toggle('is-active', index === activeIndex);
                });
            });
        }, {
            rootMargin: '-42% 0px -42%',
            threshold: 0
        });

        contentSections.forEach(section => activeSectionObserver.observe(section));
    }

    document.querySelectorAll('.home-faq-list details').forEach(details => {
        const summary = details.querySelector('summary');
        if (!summary) {
            return;
        }

        summary.addEventListener('click', event => {
            event.preventDefault();
            const shouldOpen = !details.open;

            document.querySelectorAll('.home-faq-list details[open]').forEach(openDetails => {
                if (openDetails !== details) {
                    animateDetails(openDetails, false);
                }
            });

            animateDetails(details, shouldOpen);
        });
    });

    function animateDetails(details, shouldOpen) {
        if (details.dataset.animating === 'true' || reducedMotion || !details.animate) {
            details.open = shouldOpen;
            return;
        }

        details.dataset.animating = 'true';
        const summary = details.querySelector('summary');
        const startHeight = `${details.offsetHeight}px`;

        if (shouldOpen) {
            details.open = true;
        }

        const endHeight = shouldOpen ? `${details.scrollHeight}px` : `${summary.offsetHeight}px`;
        const animation = details.animate(
            { height: [startHeight, endHeight] },
            { duration: 260, easing: 'cubic-bezier(.2, .72, .2, 1)' }
        );

        details.style.overflow = 'hidden';
        animation.onfinish = () => {
            details.open = shouldOpen;
            details.style.height = '';
            details.style.overflow = '';
            details.dataset.animating = 'false';
        };
        animation.oncancel = animation.onfinish;
    }

    if (!finePointer || reducedMotion) {
        return;
    }

    contentSections.forEach(section => {
        section.addEventListener('pointerenter', () => section.classList.add('has-pointer'));
        section.addEventListener('pointerleave', () => section.classList.remove('has-pointer'));
        section.addEventListener('pointermove', event => {
            const bounds = section.getBoundingClientRect();
            section.style.setProperty('--pointer-x', `${event.clientX - bounds.left}px`);
            section.style.setProperty('--pointer-y', `${event.clientY - bounds.top}px`);
        }, { passive: true });
    });

    document.querySelectorAll('.feature-card, .home-step, .home-audience-card, .route-map-card').forEach(card => {
        card.classList.add('home-tilt');
        let tiltFrame = null;

        card.addEventListener('pointermove', event => {
            if (tiltFrame !== null) {
                return;
            }

            tiltFrame = window.requestAnimationFrame(() => {
                tiltFrame = null;
                const bounds = card.getBoundingClientRect();
                const horizontal = (event.clientX - bounds.left) / bounds.width - 0.5;
                const vertical = (event.clientY - bounds.top) / bounds.height - 0.5;
                card.style.setProperty('--tilt-x', `${vertical * -5}deg`);
                card.style.setProperty('--tilt-y', `${horizontal * 5}deg`);
                card.classList.add('is-tilting');
            });
        });

        card.addEventListener('pointerleave', () => {
            card.classList.remove('is-tilting');
            card.style.removeProperty('--tilt-x');
            card.style.removeProperty('--tilt-y');
        });
    });
});
