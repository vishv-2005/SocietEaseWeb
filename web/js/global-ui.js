(function () {
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    document.documentElement.classList.add('ui-enhanced');

    // --- Ambient Shell ---
    const shell = document.createElement('div');
    shell.className = 'ambient-shell';
    shell.setAttribute('aria-hidden', 'true');
    shell.innerHTML = '<span></span><span></span><span></span><span></span>';
    document.body.prepend(shell);

    // --- Reveal-on-Scroll ---
    const revealTargets = document.querySelectorAll(
        '.page-header, .stat-card, .card, .helper-card, .auth-card, .wizard-steps, .alert, .search-box, table'
    );

    revealTargets.forEach((element, index) => {
        element.classList.add('reveal-surface');
        element.style.setProperty('--reveal-delay', Math.min(index * 55, 450) + 'ms');
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

    // --- Enhanced Tilt Surfaces ---
    if (!prefersReducedMotion) {
        document.querySelectorAll('.stat-card, .card, .auth-card, .helper-card').forEach((element) => {
            element.classList.add('tilt-surface');

            element.addEventListener('pointermove', (event) => {
                const rect = element.getBoundingClientRect();
                const x = ((event.clientX - rect.left) / rect.width - 0.5) * 10;
                const y = ((event.clientY - rect.top) / rect.height - 0.5) * -10;
                element.style.setProperty('--tilt-x', y.toFixed(2) + 'deg');
                element.style.setProperty('--tilt-y', x.toFixed(2) + 'deg');
            });

            element.addEventListener('pointerleave', () => {
                element.style.setProperty('--tilt-x', '0deg');
                element.style.setProperty('--tilt-y', '0deg');
            });
        });
    }

    // --- Sidebar Cursor Spotlight ---
    document.querySelectorAll('.sidebar-nav a').forEach((link) => {
        link.addEventListener('pointermove', (event) => {
            const rect = link.getBoundingClientRect();
            const x = Math.round(event.clientX - rect.left);
            const y = Math.round(event.clientY - rect.top);
            link.style.setProperty('--spot-x', x + 'px');
            link.style.setProperty('--spot-y', y + 'px');
        });
    });

    // --- Magnetic Buttons ---
    if (!prefersReducedMotion) {
        document.querySelectorAll('.btn').forEach((btn) => {
            btn.addEventListener('pointermove', (event) => {
                const rect = btn.getBoundingClientRect();
                const centerX = rect.left + rect.width / 2;
                const centerY = rect.top + rect.height / 2;
                const dx = (event.clientX - centerX) * 0.15;
                const dy = (event.clientY - centerY) * 0.15;
                btn.style.setProperty('--mag-x', dx.toFixed(1) + 'px');
                btn.style.setProperty('--mag-y', dy.toFixed(1) + 'px');
            });

            btn.addEventListener('pointerleave', () => {
                btn.style.setProperty('--mag-x', '0px');
                btn.style.setProperty('--mag-y', '0px');
            });
        });
    }

    // --- Click Ripple on Buttons ---
    document.querySelectorAll('.btn').forEach((btn) => {
        btn.addEventListener('click', (event) => {
            const ripple = document.createElement('span');
            ripple.className = 'btn-ripple';
            const rect = btn.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            ripple.style.width = ripple.style.height = size + 'px';
            ripple.style.left = (event.clientX - rect.left - size / 2) + 'px';
            ripple.style.top = (event.clientY - rect.top - size / 2) + 'px';
            btn.appendChild(ripple);
            ripple.addEventListener('animationend', () => ripple.remove());
        });
    });

    // --- Number Counter Animation ---
    if (!prefersReducedMotion && 'IntersectionObserver' in window) {
        const counterObserver = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    animateCounter(entry.target);
                    counterObserver.unobserve(entry.target);
                }
            });
        }, { threshold: 0.5 });

        document.querySelectorAll('.stat-value').forEach((el) => {
            counterObserver.observe(el);
        });
    }

    function animateCounter(element) {
        const text = element.textContent.trim();
        const num = parseInt(text, 10);
        if (isNaN(num) || num === 0 || text === '—') return;

        const duration = 1200;
        const start = performance.now();
        const prefix = text.replace(/[\d,]+/, '');
        element.textContent = '0';

        function tick(now) {
            const elapsed = now - start;
            const progress = Math.min(elapsed / duration, 1);
            // Ease-out cubic
            const eased = 1 - Math.pow(1 - progress, 3);
            const current = Math.round(eased * num);
            element.textContent = current.toLocaleString() + (prefix !== text ? '' : '');
            if (progress < 1) {
                requestAnimationFrame(tick);
            } else {
                element.textContent = text;
            }
        }

        requestAnimationFrame(tick);
    }

    // --- Scroll Parallax for Ambient Shell ---
    if (!prefersReducedMotion) {
        let ticking = false;
        window.addEventListener('scroll', () => {
            if (!ticking) {
                requestAnimationFrame(() => {
                    const scrollY = window.scrollY;
                    const spans = shell.querySelectorAll('span');
                    spans.forEach((span, i) => {
                        const speed = 0.015 + i * 0.008;
                        span.style.transform =
                            'rotateX(58deg) rotateZ(45deg) translateY(' + (-scrollY * speed).toFixed(1) + 'px)';
                    });
                    ticking = false;
                });
                ticking = true;
            }
        });
    }
    // --- Mobile Sidebar Toggle ---
    (function initMobileSidebar() {
        const sidebar = document.querySelector('.sidebar');
        if (!sidebar) return; // Only on pages with a sidebar

        // Create hamburger toggle button
        const toggle = document.createElement('button');
        toggle.className = 'sidebar-toggle';
        toggle.setAttribute('aria-label', 'Toggle navigation menu');
        toggle.innerHTML = '☰';
        document.body.appendChild(toggle);

        // Create overlay backdrop
        const overlay = document.createElement('div');
        overlay.className = 'sidebar-overlay';
        overlay.setAttribute('aria-hidden', 'true');
        document.body.appendChild(overlay);

        function openSidebar() {
            sidebar.classList.add('open');
            overlay.classList.add('active');
            toggle.innerHTML = '✕';
            toggle.setAttribute('aria-expanded', 'true');
        }

        function closeSidebar() {
            sidebar.classList.remove('open');
            overlay.classList.remove('active');
            toggle.innerHTML = '☰';
            toggle.setAttribute('aria-expanded', 'false');
        }

        // Toggle on hamburger click
        toggle.addEventListener('click', function (e) {
            e.stopPropagation();
            if (sidebar.classList.contains('open')) {
                closeSidebar();
            } else {
                openSidebar();
            }
        });

        // Close on overlay click
        overlay.addEventListener('click', closeSidebar);

        // Close when a sidebar nav link is clicked
        sidebar.querySelectorAll('.sidebar-nav a').forEach(function (link) {
            link.addEventListener('click', closeSidebar);
        });

        // Close on Escape key
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && sidebar.classList.contains('open')) {
                closeSidebar();
            }
        });

        // Auto-close if window resized past mobile breakpoint
        window.addEventListener('resize', function () {
            if (window.innerWidth > 768) {
                closeSidebar();
            }
        });
    }());
}());

