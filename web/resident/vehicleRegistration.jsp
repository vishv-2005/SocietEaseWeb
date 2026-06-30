<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Vehicle Registration — SocietEase</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
    <div class="app-layout">
        <nav class="sidebar">
            <div class="sidebar-logo">Societ<span>Ease</span></div>
            <div class="sidebar-society"><c:out value="${sessionScope.societyName}"/></div>
            <div class="sidebar-nav">
                <a href="${pageContext.request.contextPath}/resident/dashboard.jsp">📊 Dashboard</a>
                <a href="${pageContext.request.contextPath}/resident/notices.jsp">📢 Notices</a>
                <a href="${pageContext.request.contextPath}/resident/fileComplaint.jsp">📋 File Complaint</a>
                <a href="${pageContext.request.contextPath}/resident/payMaintenance.jsp">💳 Pay Maintenance</a>
                <a href="${pageContext.request.contextPath}/resident/paymentHistory.jsp">📄 Payment History</a>
                <a href="${pageContext.request.contextPath}/resident/vehicleRegistration.jsp" class="active">🚗 My Vehicles</a>
            </div>
            <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/LogoutServlet">🚪 Logout</a></div>
        </nav>

        <main class="main-content">
            <div class="page-header"><div><h1>Vehicle Registration</h1><p>Register and manage your vehicles for society parking records</p></div></div>

            <c:if test="${not empty sessionScope.message}">
                <div class="alert alert-${sessionScope.messageType == 'success' ? 'success' : 'error'}">
                    <c:out value="${sessionScope.message}"/>
                </div>
                <c:remove var="message" scope="session"/>
                <c:remove var="messageType" scope="session"/>
            </c:if>

            <div style="display:grid; grid-template-columns:1fr 1fr; gap:1.5rem; align-items:start;">

                <!-- Registration Form -->
                <div class="card">
                    <div class="card-header"><h2>➕ Register New Vehicle</h2></div>
                    <div class="card-body">
                        <form method="POST" action="${pageContext.request.contextPath}/VehicleServlet">
                            <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}">

                            <div class="form-group">
                                <label for="vehicleType">Vehicle Type *</label>
                                <select class="form-control" id="vehicleType" name="vehicleType" required onchange="updateFormFields()">
                                    <option value="">— Select Type —</option>
                                    <option value="2_WHEELER">🏍️ 2-Wheeler (Bike / Scooter)</option>
                                    <option value="4_WHEELER">🚗 4-Wheeler (Car / SUV)</option>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="regNumber">Registration Number *</label>
                                <input type="text" class="form-control" id="regNumber" name="regNumber"
                                       placeholder="e.g., GJ01AB1234" required style="text-transform:uppercase;">
                            </div>

                            <div class="form-group">
                                <label for="ownerName">Owner Name *</label>
                                <input type="text" class="form-control" id="ownerName" name="ownerName"
                                       value="<c:out value='${sessionScope.userName}'/>" required>
                            </div>

                            <div class="form-group">
                                <label for="make">Make / Brand</label>
                                <input type="text" class="form-control" id="make" name="make"
                                       placeholder="e.g., Honda, Maruti, TVS">
                            </div>

                            <div class="form-group">
                                <label for="model">Model</label>
                                <input type="text" class="form-control" id="model" name="model"
                                       placeholder="e.g., Activa, Swift, Apache">
                            </div>

                            <div class="form-group">
                                <label for="color">Color</label>
                                <input type="text" class="form-control" id="color" name="color"
                                       placeholder="e.g., White, Black, Red">
                            </div>

                            <div class="form-group">
                                <label for="parkingSlot">Parking Slot (if assigned)</label>
                                <input type="text" class="form-control" id="parkingSlot" name="parkingSlot"
                                       placeholder="e.g., P-101, B1-05">
                            </div>

                            <button type="submit" class="btn btn-primary btn-lg" style="width:100%;">
                                Register Vehicle
                            </button>
                        </form>
                    </div>
                </div>

                <!-- Registered Vehicles List -->
                <div class="card">
                    <div class="card-header"><h2>🚗 My Registered Vehicles</h2></div>
                    <div class="card-body" id="vehicle-list">
                        <p style="color:var(--text-muted);">Loading vehicles...</p>
                    </div>
                </div>

            </div>
        </main>
    </div>

    <script>
        // Load registered vehicles
        fetch('${pageContext.request.contextPath}/VehicleServlet')
            .then(r => r.json())
            .then(data => {
                const vl = document.getElementById('vehicle-list');
                if (data.vehicles && data.vehicles.length > 0) {
                    vl.innerHTML = data.vehicles.map(v => {
                        const icon = v.type === '2_WHEELER' ? '🏍️' : '🚗';
                        const typeLabel = v.type === '2_WHEELER' ? '2-Wheeler' : '4-Wheeler';
                        return '<div style="padding:1rem;border:1px solid var(--border);border-radius:var(--radius-sm);margin-bottom:0.75rem;background:var(--primary-bg);">' +
                            '<div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:0.5rem;">' +
                            '<span style="font-size:1.1rem;font-weight:700;">' + icon + ' ' + escapeHtml(v.regNumber) + '</span>' +
                            '<span class="badge badge-info">' + typeLabel + '</span></div>' +
                            '<div style="font-size:0.9rem;color:var(--text-secondary);display:grid;grid-template-columns:1fr 1fr;gap:0.25rem;">' +
                            (v.make || v.model ? '<div><strong>Vehicle:</strong> ' + escapeHtml(v.make) + ' ' + escapeHtml(v.model) + '</div>' : '') +
                            (v.color ? '<div><strong>Color:</strong> ' + escapeHtml(v.color) + '</div>' : '') +
                            '<div><strong>Owner:</strong> ' + escapeHtml(v.owner) + '</div>' +
                            (v.parking ? '<div><strong>Parking:</strong> ' + escapeHtml(v.parking) + '</div>' : '') +
                            '<div><strong>Registered:</strong> ' + escapeHtml(v.date) + '</div>' +
                            '</div></div>';
                    }).join('');
                } else {
                    vl.innerHTML = '<p style="color:var(--text-muted);text-align:center;padding:2rem;">No vehicles registered yet. Use the form to add your first vehicle.</p>';
                }
            }).catch(() => {
                document.getElementById('vehicle-list').innerHTML = '<p style="color:var(--text-muted);">Could not load vehicles.</p>';
            });

        function escapeHtml(t) { if(!t) return ''; const d=document.createElement('div'); d.textContent=t; return d.innerHTML; }
    </script>
</body>
</html>
