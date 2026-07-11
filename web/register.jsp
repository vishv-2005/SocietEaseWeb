<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Register your residential society on SocietEase in under 5 minutes. Apartments, villas, duplexes, row houses — set up and manage them all.">
    <meta name="robots" content="index, follow">
    <title>Register Your Society — SocietEase</title>
    <link rel="canonical" href="https://societease.online/register.jsp">
    <link rel="icon" href="favicon.ico" type="image/x-icon">

    <!-- Open Graph / Social Sharing -->
    <meta property="og:type" content="website">
    <meta property="og:title" content="Register Your Society — SocietEase">
    <meta property="og:description" content="Set up any type of residential society in under 5 minutes. Apartments, villas, duplexes, row houses — we handle them all.">
    <meta property="og:url" content="https://societease.online/register.jsp">
    <meta property="og:image" content="https://societease.online/images/og-preview.jpg">
    <meta property="og:site_name" content="SocietEase">

    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
    <main class="auth-page">
        <div class="auth-left">
            <div>
                <p class="auth-left-heading" style="font-size:1.75rem;font-weight:700;line-height:1.3;">Register Your<br>Society on Societ<span style="color:#a5b4fc">Ease</span></p>
                <p>Set up any type of residential society in under 5 minutes. Apartments, villas, duplexes, row houses — we handle them all.</p>
            </div>
        </div>
        <div class="auth-right" style="overflow-y:auto;">
            <div class="auth-card" style="max-width:540px;">
                <h1>Society Registration</h1>
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
                        <span class="step-number">2</span>Structure
                    </div>
                    <div class="wizard-connector" id="wc-2"></div>
                    <div class="wizard-step" id="ws-3">
                        <span class="step-number">3</span>Account
                    </div>
                </div>

                <form action="RegisterSocietyServlet" method="POST" id="registerForm">
                    <!-- Step 1: Society Details + Type Selection -->
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

                        <!-- Society Type Selection -->
                        <label style="font-weight:600; font-size:0.875rem; margin-bottom:0.6rem; display:block;">Society Type *</label>
                        <div class="type-selector">
                            <div class="type-card selected" data-type="APARTMENT" onclick="selectType(this)">
                                <span class="type-icon">🏢</span>
                                <span class="type-name">Apartment / Tower</span>
                                <span class="type-desc">Towers with floors & units per floor</span>
                            </div>
                            <div class="type-card" data-type="INDIVIDUAL" onclick="selectType(this)">
                                <span class="type-icon">🏘️</span>
                                <span class="type-name">Individual Units</span>
                                <span class="type-desc">Villas, bungalows, duplexes, row houses, plots...</span>
                            </div>
                        </div>
                        <input type="hidden" name="societyType" id="societyType" value="APARTMENT">

                        <button type="button" class="btn btn-primary btn-lg" style="width:100%;" onclick="goToStep(2)">
                            Next: Building Configuration →
                        </button>
                    </div>

                    <!-- Step 2: Building Configuration (Dynamic) -->
                    <div class="wizard-panel" id="panel-2">

                        <!-- APARTMENT Configuration -->
                        <div id="config-apartment" class="config-section">
                            <div class="alert alert-warning" style="margin-bottom:1.5rem;">
                                ⚠️ The number of towers and apartments <strong>cannot be changed later</strong>.
                                Empty apartments will be marked as vacant.
                            </div>

                            <div class="form-group">
                                <label for="totalTowers">Number of Towers (A, B, C...) *</label>
                                <input type="number" class="form-control" id="totalTowers" name="totalTowers"
                                       min="1" max="26" value="1" onchange="updatePreview()">
                            </div>
                            <div class="form-row">
                                <div class="form-group">
                                    <label for="floorsPerTower">Floors per Tower *</label>
                                    <input type="number" class="form-control" id="floorsPerTower" name="floorsPerTower"
                                           min="1" max="50" value="5" onchange="updatePreview()">
                                </div>
                                <div class="form-group">
                                    <label for="unitsPerFloor">Units per Floor *</label>
                                    <input type="number" class="form-control" id="unitsPerFloor" name="unitsPerFloor"
                                           min="1" max="20" value="4" onchange="updatePreview()">
                                </div>
                            </div>
                        </div>

                        <!-- INDIVIDUAL Configuration -->
                        <div id="config-individual" class="config-section" style="display:none;">
                            <div class="alert alert-warning" style="margin-bottom:1.5rem;">
                                ⚠️ The unit structure <strong>cannot be changed later</strong>.
                                Unused units will be marked as vacant.
                            </div>

                            <div class="form-group">
                                <label for="unitLabel">What are your units called? *</label>
                                <select class="form-control" id="unitLabel" name="unitLabel" onchange="handleUnitLabelChange(); updatePreview();">
                                    <option value="House">House</option>
                                    <option value="Villa">Villa</option>
                                    <option value="Bungalow">Bungalow</option>
                                    <option value="Duplex">Duplex</option>
                                    <option value="Row House">Row House</option>
                                    <option value="Plot">Plot</option>
                                    <option value="Room">Room</option>
                                    <option value="Flat">Flat</option>
                                    <option value="Unit">Unit</option>
                                    <option value="__custom__">Other (custom)...</option>
                                </select>
                            </div>
                            <div class="form-group" id="customUnitLabelGroup" style="display:none;">
                                <label for="customUnitLabel">Custom Unit Name *</label>
                                <input type="text" class="form-control" id="customUnitLabel" name="customUnitLabel"
                                       placeholder="e.g., Cottage, Tenement" onchange="updatePreview()">
                            </div>

                            <!-- Blocks Toggle -->
                            <div class="toggle-group">
                                <label for="hasBlocks">Does your society have blocks / wings / sectors?</label>
                                <input type="checkbox" class="toggle-switch" id="hasBlocks" name="hasBlocks" onchange="toggleBlocks(); updatePreview();">
                            </div>

                            <!-- Block Config (hidden by default) -->
                            <div id="blockConfig" style="display:none;">
                                <div class="form-group">
                                    <label for="blockLabel">What are they called? *</label>
                                    <select class="form-control" id="blockLabel" name="blockLabel" onchange="handleBlockLabelChange(); updatePreview();">
                                        <option value="Block">Block</option>
                                        <option value="Wing">Wing</option>
                                        <option value="Sector">Sector</option>
                                        <option value="Phase">Phase</option>
                                        <option value="__custom__">Other (custom)...</option>
                                    </select>
                                </div>
                                <div class="form-group" id="customBlockLabelGroup" style="display:none;">
                                    <label for="customBlockLabel">Custom Group Name *</label>
                                    <input type="text" class="form-control" id="customBlockLabel" name="customBlockLabel"
                                           placeholder="e.g., Zone, Area" onchange="updatePreview()">
                                </div>
                                <div class="form-row">
                                    <div class="form-group">
                                        <label for="totalBlocks">Number of Blocks *</label>
                                        <input type="number" class="form-control" id="totalBlocks" name="totalBlocks"
                                               min="1" max="26" value="2" onchange="updatePreview()">
                                    </div>
                                    <div class="form-group">
                                        <label for="unitsPerBlock">Units per Block *</label>
                                        <input type="number" class="form-control" id="unitsPerBlock" name="unitsPerBlock"
                                               min="1" max="200" value="10" onchange="updatePreview()">
                                    </div>
                                </div>
                            </div>

                            <!-- No blocks: just total units -->
                            <div id="noBlockConfig">
                                <div class="form-group">
                                    <label for="totalUnits">Total Number of Units *</label>
                                    <input type="number" class="form-control" id="totalUnits" name="totalUnits"
                                           min="1" max="500" value="20" onchange="updatePreview()">
                                </div>
                            </div>

                            <!-- Numbering Style -->
                            <div class="form-group">
                                <label for="numberingStyle">Numbering Style *</label>
                                <select class="form-control" id="numberingStyle" name="numberingStyle" onchange="updatePreview()">
                                    <option value="PREFIX">With prefix (Villa-1, Plot-2)</option>
                                    <option value="PLAIN">Plain numbers (1, 2, 3)</option>
                                </select>
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
    </main>

    <script>
        let currentType = 'APARTMENT';

        // --- Society Type Selection ---
        function selectType(card) {
            document.querySelectorAll('.type-card').forEach(c => c.classList.remove('selected'));
            card.classList.add('selected');
            currentType = card.getAttribute('data-type');
            document.getElementById('societyType').value = currentType;

            // Toggle config sections
            document.getElementById('config-apartment').style.display = currentType === 'APARTMENT' ? '' : 'none';
            document.getElementById('config-individual').style.display = currentType === 'INDIVIDUAL' ? '' : 'none';

            // Set required attributes appropriately
            const aptFields = document.querySelectorAll('#config-apartment input');
            const indFields = document.querySelectorAll('#config-individual input, #config-individual select');

            if (currentType === 'APARTMENT') {
                aptFields.forEach(f => f.setAttribute('required', ''));
                document.getElementById('totalTowers').setAttribute('required', '');
                document.getElementById('floorsPerTower').setAttribute('required', '');
                document.getElementById('unitsPerFloor').setAttribute('required', '');
            } else {
                aptFields.forEach(f => f.removeAttribute('required'));
            }

            updatePreview();
        }

        // --- Toggle Blocks Section ---
        function toggleBlocks() {
            const hasBlocks = document.getElementById('hasBlocks').checked;
            document.getElementById('blockConfig').style.display = hasBlocks ? '' : 'none';
            document.getElementById('noBlockConfig').style.display = hasBlocks ? 'none' : '';

            // Update numbering style options
            const numStyle = document.getElementById('numberingStyle');
            if (hasBlocks) {
                numStyle.innerHTML =
                    '<option value="BLOCK_UNIT">Block-Unit (A-1, B-3)</option>' +
                    '<option value="FULL_LABEL">Full label (Block-A-Villa-1)</option>' +
                    '<option value="PLAIN">Plain sequential (1, 2, 3...)</option>';
            } else {
                numStyle.innerHTML =
                    '<option value="PREFIX">With prefix (Villa-1, Plot-2)</option>' +
                    '<option value="PLAIN">Plain numbers (1, 2, 3)</option>';
            }
        }

        // --- Custom Label Handlers ---
        function handleUnitLabelChange() {
            const val = document.getElementById('unitLabel').value;
            document.getElementById('customUnitLabelGroup').style.display = val === '__custom__' ? '' : 'none';
        }

        function handleBlockLabelChange() {
            const val = document.getElementById('blockLabel').value;
            document.getElementById('customBlockLabelGroup').style.display = val === '__custom__' ? '' : 'none';
        }

        // --- Get effective labels ---
        function getEffectiveUnitLabel() {
            const sel = document.getElementById('unitLabel');
            if (!sel) return 'Unit';
            return sel.value === '__custom__'
                ? (document.getElementById('customUnitLabel').value.trim() || 'Unit')
                : sel.value;
        }

        function getEffectiveBlockLabel() {
            const sel = document.getElementById('blockLabel');
            if (!sel) return 'Block';
            return sel.value === '__custom__'
                ? (document.getElementById('customBlockLabel').value.trim() || 'Block')
                : sel.value;
        }

        // --- Wizard Navigation ---
        function goToStep(step) {
            // Validate current step
            if (step > 1) {
                const panel1Fields = document.querySelectorAll('#panel-1 [required]');
                for (let field of panel1Fields) {
                    if (!field.value.trim()) { field.focus(); return; }
                }
            }
            if (step > 2) {
                // Validate step 2 based on type
                if (currentType === 'APARTMENT') {
                    const aptFields = ['totalTowers', 'floorsPerTower', 'unitsPerFloor'];
                    for (let id of aptFields) {
                        const f = document.getElementById(id);
                        if (!f.value || parseInt(f.value) < 1) { f.focus(); return; }
                    }
                } else {
                    const hasBlocks = document.getElementById('hasBlocks').checked;
                    if (hasBlocks) {
                        const blockFields = ['totalBlocks', 'unitsPerBlock'];
                        for (let id of blockFields) {
                            const f = document.getElementById(id);
                            if (!f.value || parseInt(f.value) < 1) { f.focus(); return; }
                        }
                    } else {
                        const f = document.getElementById('totalUnits');
                        if (!f.value || parseInt(f.value) < 1) { f.focus(); return; }
                    }
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

        // --- Live Preview ---
        function updatePreview() {
            const previewText = document.getElementById('preview-text');
            const previewLabels = document.getElementById('preview-labels');

            if (currentType === 'APARTMENT') {
                const towers = parseInt(document.getElementById('totalTowers').value) || 1;
                const floors = parseInt(document.getElementById('floorsPerTower').value) || 1;
                const units = parseInt(document.getElementById('unitsPerFloor').value) || 1;
                const total = towers * floors * units;

                previewText.innerHTML =
                    towers + ' Tower' + (towers > 1 ? 's' : '') +
                    ' &times; ' + floors + ' Floor' + (floors > 1 ? 's' : '') +
                    ' &times; ' + units + ' Unit' + (units > 1 ? 's' : '') +
                    ' = <strong>' + total + ' apartments</strong>';

                const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
                let first = letters[0] + '-101';
                let last = letters[Math.min(towers - 1, 25)] + '-' + floors + String(units).padStart(2, '0');
                previewLabels.textContent = 'Labels: ' + first + ', ' + first.slice(0,-1) + '2 ... ' + last;
            } else {
                const unitName = getEffectiveUnitLabel();
                const hasBlocks = document.getElementById('hasBlocks').checked;
                const numStyle = document.getElementById('numberingStyle').value;

                if (hasBlocks) {
                    const blockName = getEffectiveBlockLabel();
                    const blocks = parseInt(document.getElementById('totalBlocks').value) || 1;
                    const perBlock = parseInt(document.getElementById('unitsPerBlock').value) || 1;
                    const total = blocks * perBlock;
                    const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';

                    previewText.innerHTML =
                        blocks + ' ' + blockName + (blocks > 1 ? 's' : '') +
                        ' &times; ' + perBlock + ' ' + unitName + (perBlock > 1 ? 's' : '') +
                        ' = <strong>' + total + ' units</strong>';

                    let samples = [];
                    if (numStyle === 'BLOCK_UNIT') {
                        samples = [letters[0] + '-1', letters[0] + '-2'];
                        if (blocks > 1) samples.push(letters[Math.min(blocks-1,25)] + '-' + perBlock);
                    } else if (numStyle === 'FULL_LABEL') {
                        const abbr = unitName.replace(/\s+/g, '').substring(0, 3);
                        samples = [blockName + '-A-' + abbr + '1', blockName + '-A-' + abbr + '2'];
                        if (blocks > 1) samples.push(blockName + '-' + letters[Math.min(blocks-1,25)] + '-' + abbr + perBlock);
                    } else {
                        samples = ['1', '2', '3'];
                        if (total > 3) samples.push('... ' + total);
                    }
                    previewLabels.textContent = 'Labels: ' + samples.join(', ');
                } else {
                    const total = parseInt(document.getElementById('totalUnits').value) || 1;

                    previewText.innerHTML = '<strong>' + total + ' ' + unitName + (total > 1 ? 's' : '') + '</strong>';

                    let samples = [];
                    if (numStyle === 'PREFIX') {
                        const abbr = unitName.replace(/\s+/g, '-');
                        samples = [abbr + '-1', abbr + '-2', abbr + '-3'];
                        if (total > 3) samples.push('... ' + abbr + '-' + total);
                    } else {
                        samples = ['1', '2', '3'];
                        if (total > 3) samples.push('... ' + total);
                    }
                    previewLabels.textContent = 'Labels: ' + samples.join(', ');
                }
            }
        }

        // --- Form Validation ---
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

            // Set effective labels into hidden fields before submit
            if (currentType === 'INDIVIDUAL') {
                const unitLabel = document.getElementById('unitLabel');
                if (unitLabel.value === '__custom__') {
                    unitLabel.value = document.getElementById('customUnitLabel').value.trim() || 'Unit';
                }
                const blockLabel = document.getElementById('blockLabel');
                if (blockLabel && blockLabel.value === '__custom__') {
                    blockLabel.value = document.getElementById('customBlockLabel').value.trim() || 'Block';
                }
            }
        });

        // Initialize preview
        updatePreview();
    </script>
    <script src="js/global-ui.js" defer></script>
</body>
</html>
