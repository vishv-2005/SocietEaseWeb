<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register Your Society — SocietEase</title>
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
    <div class="auth-page">
        <div class="auth-left">
            <div>
                <h2>Register Your<br>Society on Societ<span style="color:#a5b4fc">Ease</span></h2>
                <p>Set up your residential society in under 5 minutes. We'll auto-generate all towers and apartments for you.</p>
            </div>
        </div>
        <div class="auth-right" style="overflow-y:auto;">
            <div class="auth-card" style="max-width:520px;">
                <h2>Society Registration</h2>
                <p class="subtitle">Fill in details to get started.</p>

                <c:if test="${not empty error}">
                    <div class="alert alert-error">⚠️ <c:out value="${error}"/></div>
                </c:if>

                <!-- Wizard Steps -->
                <div class="wizard-steps">
                    <div class="wizard-step active" id="ws-1">
                        <span class="step-number">1</span>Society
                    </div>
                    <div class="wizard-connector" id="wc-1"></div>
                    <div class="wizard-step" id="ws-2">
                        <span class="step-number">2</span>Building
                    </div>
                    <div class="wizard-connector" id="wc-2"></div>
                    <div class="wizard-step" id="ws-3">
                        <span class="step-number">3</span>Account
                    </div>
                </div>

                <form action="RegisterSocietyServlet" method="POST" id="registerForm">
                    <!-- Step 1: Society Details -->
                    <div class="wizard-panel active" id="panel-1">
                        <div class="form-group">
                            <label for="societyName">Society Name *</label>
                            <input type="text" class="form-control" id="societyName" name="societyName"
                                   placeholder="e.g., Sunshine Heights" required>
                        </div>
                        <div class="form-group">
                            <label for="address">Full Address *</label>
                            <textarea class="form-control" id="address" name="address"
                                      placeholder="Street address, area, landmark" required style="min-height:70px;"></textarea>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="city">City *</label>
                                <input type="text" class="form-control" id="city" name="city" placeholder="City" required>
                            </div>
                            <div class="form-group">
                                <label for="state">State *</label>
                                <input type="text" class="form-control" id="state" name="state" placeholder="State" required>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="pincode">Pincode *</label>
                            <input type="text" class="form-control" id="pincode" name="pincode"
                                   placeholder="e.g., 380015" maxlength="10" required>
                        </div>
                        <button type="button" class="btn btn-primary btn-lg" style="width:100%;" onclick="goToStep(2)">
                            Next: Building Configuration →
                        </button>
                    </div>

                    <!-- Step 2: Building Configuration -->
                    <div class="wizard-panel" id="panel-2">
                        <div class="alert alert-warning" style="margin-bottom:1.5rem;">
                            ⚠️ The number of towers and apartments <strong>cannot be changed later</strong>.
                            Empty apartments will be marked as vacant.
                        </div>

                        <div class="form-group">
                            <label for="totalTowers">Number of Towers (A, B, C...) *</label>
                            <input type="number" class="form-control" id="totalTowers" name="totalTowers"
                                   min="1" max="26" value="1" required onchange="updatePreview()">
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="floorsPerTower">Floors per Tower *</label>
                                <input type="number" class="form-control" id="floorsPerTower" name="floorsPerTower"
                                       min="1" max="50" value="5" required onchange="updatePreview()">
                            </div>
                            <div class="form-group">
                                <label for="unitsPerFloor">Units per Floor *</label>
                                <input type="number" class="form-control" id="unitsPerFloor" name="unitsPerFloor"
                                       min="1" max="20" value="4" required onchange="updatePreview()">
                            </div>
                        </div>

                        <!-- Preview -->
                        <div style="background:var(--primary-bg); border-radius:var(--radius-sm); padding:1.25rem; margin:1.5rem 0;">
                            <div style="font-weight:600; color:var(--primary); margin-bottom:0.5rem;">📊 Preview</div>
                            <div id="preview-text" style="font-size:0.9rem; color:var(--text);">
                                1 Tower × 5 Floors × 4 Units = <strong>20 apartments</strong>
                            </div>
                            <div id="preview-labels" style="font-size:0.8rem; color:var(--text-secondary); margin-top:0.5rem;">
                                Labels: A-101, A-102, A-103, A-104 ... A-504
                            </div>
                        </div>

                        <div style="display:flex; gap:0.75rem;">
                            <button type="button" class="btn btn-secondary btn-lg" style="flex:1;" onclick="goToStep(1)">
                                ← Back
                            </button>
                            <button type="button" class="btn btn-primary btn-lg" style="flex:2;" onclick="goToStep(3)">
                                Next: Your Account →
                            </button>
                        </div>
                    </div>

                    <!-- Step 3: RP Account -->
                    <div class="wizard-panel" id="panel-3">
                        <div class="form-group">
                            <label for="rpName">Your Full Name (Responsible Person) *</label>
                            <input type="text" class="form-control" id="rpName" name="rpName"
                                   placeholder="e.g., Raj Patel" required>
                        </div>
                        <div class="form-group">
                            <label for="rpEmail">Your Email Address *</label>
                            <input type="email" class="form-control" id="rpEmail" name="rpEmail"
                                   placeholder="you@example.com" required>
                        </div>
                        <div class="form-group">
                            <label for="rpPassword">Create Password *</label>
                            <input type="password" class="form-control" id="rpPassword" name="rpPassword"
                                   placeholder="Min 8 chars, uppercase, lowercase, digit" required minlength="8">
                            <div class="form-error" id="passwordError" style="display:none;"></div>
                        </div>
                        <div class="form-group">
                            <label for="rpPasswordConfirm">Confirm Password *</label>
                            <input type="password" class="form-control" id="rpPasswordConfirm"
                                   placeholder="Re-enter your password" required>
                        </div>

                        <div style="display:flex; gap:0.75rem;">
                            <button type="button" class="btn btn-secondary btn-lg" style="flex:1;" onclick="goToStep(2)">
                                ← Back
                            </button>
                            <button type="submit" class="btn btn-success btn-lg" style="flex:2;">
                                ✓ Register Society
                            </button>
                        </div>
                    </div>
                </form>

                <p style="text-align:center; margin-top:1.5rem; color: var(--text-secondary); font-size: 0.9rem;">
                    Already registered? <a href="login.jsp" style="font-weight:600;">Log In</a>
                </p>
            </div>
        </div>
    </div>

    <script>
        function goToStep(step) {
            // Validate current step
            if (step > 1) {
                const panel1Fields = document.querySelectorAll('#panel-1 [required]');
                for (let field of panel1Fields) {
                    if (!field.value.trim()) { field.focus(); return; }
                }
            }
            if (step > 2) {
                const panel2Fields = document.querySelectorAll('#panel-2 [required]');
                for (let field of panel2Fields) {
                    if (!field.value.trim()) { field.focus(); return; }
                }
            }

            // Update panels
            document.querySelectorAll('.wizard-panel').forEach(p => p.classList.remove('active'));
            document.getElementById('panel-' + step).classList.add('active');

            // Update steps
            for (let i = 1; i <= 3; i++) {
                const ws = document.getElementById('ws-' + i);
                ws.classList.remove('active', 'completed');
                if (i < step) ws.classList.add('completed');
                if (i === step) ws.classList.add('active');
            }
            for (let i = 1; i <= 2; i++) {
                const wc = document.getElementById('wc-' + i);
                wc.classList.toggle('completed', i < step);
            }
        }

        function updatePreview() {
            const towers = parseInt(document.getElementById('totalTowers').value) || 1;
            const floors = parseInt(document.getElementById('floorsPerTower').value) || 1;
            const units = parseInt(document.getElementById('unitsPerFloor').value) || 1;
            const total = towers * floors * units;

            document.getElementById('preview-text').innerHTML =
                towers + ' Tower' + (towers > 1 ? 's' : '') +
                ' &times; ' + floors + ' Floor' + (floors > 1 ? 's' : '') +
                ' &times; ' + units + ' Unit' + (units > 1 ? 's' : '') +
                ' = <strong>' + total + ' apartments</strong>';

            const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
            let first = letters[0] + '-101';
            let last = letters[Math.min(towers - 1, 25)] + '-' + floors + String(units).padStart(2, '0');
            document.getElementById('preview-labels').textContent =
                'Labels: ' + first + ' ... ' + last;
        }

        // Form validation
        document.getElementById('registerForm').addEventListener('submit', function(e) {
            const pass = document.getElementById('rpPassword').value;
            const confirm = document.getElementById('rpPasswordConfirm').value;
            const err = document.getElementById('passwordError');

            if (pass !== confirm) {
                e.preventDefault();
                err.textContent = 'Passwords do not match.';
                err.style.display = 'block';
                return;
            }

            if (pass.length < 8 || !/[A-Z]/.test(pass) || !/[a-z]/.test(pass) || !/[0-9]/.test(pass)) {
                e.preventDefault();
                err.textContent = 'Password needs 8+ chars with uppercase, lowercase, and digit.';
                err.style.display = 'block';
                return;
            }

            err.style.display = 'none';
        });
    </script>
</body>
</html>
