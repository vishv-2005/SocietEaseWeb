(function () {
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    document.documentElement.classList.add('ui-enhanced');

    const shell = document.createElement('div');
    shell.className = 'ambient-shell';
    shell.setAttribute('aria-hidden', 'true');
    shell.innerHTML = '<span></span><span></span><span></span><span></span>';
    document.body.prepend(shell);

    const revealTargets = document.querySelectorAll(
        '.page-header, .stat-card, .card, .helper-card, .auth-card, .wizard-steps, .alert, .search-box, table'
    );

    revealTargets.forEach((element, index) => {
        element.classList.add('reveal-surface');
        element.style.setProperty('--reveal-delay', Math.min(index * 45, 360) + 'ms');
    });

    if (!prefersReducedMotion && 'IntersectionObserver' in window) {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('is-visible');
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.08 });

        revealTargets.forEach((element) => observer.observe(element));
    } else {
        revealTargets.forEach((element) => element.classList.add('is-visible'));
    }

    if (!prefersReducedMotion) {
        document.querySelectorAll('.stat-card, .card, .auth-card, .helper-card').forEach((element) => {
            element.classList.add('tilt-surface');

            element.addEventListener('pointermove', (event) => {
                const rect = element.getBoundingClientRect();
                const x = ((event.clientX - rect.left) / rect.width - 0.5) * 8;
                const y = ((event.clientY - rect.top) / rect.height - 0.5) * -8;
                element.style.setProperty('--tilt-x', y.toFixed(2) + 'deg');
                element.style.setProperty('--tilt-y', x.toFixed(2) + 'deg');
            });

            element.addEventListener('pointerleave', () => {
                element.style.setProperty('--tilt-x', '0deg');
                element.style.setProperty('--tilt-y', '0deg');
            });
        });
    }

    document.querySelectorAll('.sidebar-nav a').forEach((link) => {
        link.addEventListener('pointermove', (event) => {
            const rect = link.getBoundingClientRect();
            const x = Math.round(event.clientX - rect.left);
            const y = Math.round(event.clientY - rect.top);
            link.style.setProperty('--spot-x', x + 'px');
            link.style.setProperty('--spot-y', y + 'px');
        });
    });
}());
