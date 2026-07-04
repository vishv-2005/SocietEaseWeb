<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="SocietEase - modern society management for residents, maintenance, complaints, notices, and secure operations.">
    <title>SocietEase - Society Management Made Easy</title>
    <link rel="stylesheet" href="css/styles.css">
</head>
<body class="landing-body">
    <main class="landing-hero landing-hero-3d">
        <canvas id="societyScene" class="society-scene" aria-hidden="true"></canvas>
        <div class="landing-grid" aria-hidden="true"></div>

        <nav class="landing-nav" aria-label="Primary">
            <a href="index.jsp" class="landing-brand" aria-label="SocietEase home">
                <span class="brand-mark">SE</span>
                <span>Societ<span>Ease</span></span>
            </a>
            <div class="landing-nav-actions">
                <a href="login.jsp" class="nav-link">Login</a>
                <a href="register.jsp" class="btn btn-white btn-sm">Register</a>
            </div>
        </nav>

        <section class="landing-stage">
            <div class="landing-copy">
                <p class="landing-kicker">Multi-tenant residential operations</p>
                <h1>SocietEase</h1>
                <p class="landing-lede">
                    Register a society, generate towers and apartments, manage residents,
                    collect maintenance, resolve complaints, and send notices from one secure dashboard.
                </p>

                <div class="landing-actions">
                    <a href="register.jsp" class="btn btn-white btn-lg" id="btn-register">Register Your Society</a>
                    <a href="login.jsp" class="btn btn-outline btn-lg" id="btn-login">Login</a>
                </div>

                <div class="landing-metrics" aria-label="Platform capabilities">
                    <div>
                        <strong>13</strong>
                        <span>auto-managed database tables</span>
                    </div>
                    <div>
                        <strong>AES</strong>
                        <span>encrypted resident records</span>
                    </div>
                    <div>
                        <strong>24/7</strong>
                        <span>notices, complaints, and payments</span>
                    </div>
                </div>
            </div>

            <aside class="landing-command-panel" aria-label="SocietEase workflow snapshot">
                <div class="panel-header">
                    <span>Live Society Snapshot</span>
                    <span class="status-dot">Online</span>
                </div>
                <div class="mini-map">
                    <div class="tower tower-a">
                        <span></span><span></span><span></span><span></span>
                        <span></span><span></span><span></span><span></span>
                    </div>
                    <div class="tower tower-b">
                        <span></span><span></span><span></span><span></span>
                        <span></span><span></span><span></span><span></span>
                    </div>
                    <div class="tower tower-c">
                        <span></span><span></span><span></span><span></span>
                        <span></span><span></span><span></span><span></span>
                    </div>
                </div>
                <div class="workflow-list">
                    <div>
                        <span class="workflow-icon">01</span>
                        <p><strong>Residents</strong><small>Encrypted profiles and apartments</small></p>
                    </div>
                    <div>
                        <span class="workflow-icon">02</span>
                        <p><strong>Maintenance</strong><small>Razorpay-ready fee collection</small></p>
                    </div>
                    <div>
                        <span class="workflow-icon">03</span>
                        <p><strong>Community</strong><small>Notices, helpers, committees</small></p>
                    </div>
                </div>
            </aside>
        </section>

        <section class="landing-feature-strip" aria-label="Key features">
            <article>
                <span>Blocks</span>
                <strong>Dynamic tower setup</strong>
            </article>
            <article>
                <span>Payments</span>
                <strong>Maintenance tracking</strong>
            </article>
            <article>
                <span>Security</span>
                <strong>CSRF and encrypted PII</strong>
            </article>
            <article>
                <span>Alerts</span>
                <strong>Email-based updates</strong>
            </article>
        </section>
    </main>

    <script type="module" src="js/landing-3d.js"></script>
    <script src="js/global-ui.js" defer></script>
</body>
</html>
