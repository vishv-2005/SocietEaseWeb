<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard — SocietEase</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
    <div class="app-layout">
        <!-- Sidebar -->
        <nav class="sidebar">
            <div class="sidebar-logo">Societ<span>Ease</span></div>
            <div class="sidebar-society">
                <c:out value="${sessionScope.societyName}"/> &bull; RP: <c:out value="${sessionScope.userName}"/>
            </div>
            <div class="sidebar-nav">
                <a href="${pageContext.request.contextPath}/adminDashboardData?view=dashboard" class="active" id="nav-dashboard">📊 Dashboard</a>
                <a href="${pageContext.request.contextPath}/ManageResidentServlet" id="nav-residents">🏠 Manage Residents</a>
                <a href="${pageContext.request.contextPath}/ManageHelperServlet" id="nav-helpers">👷 Manage Helpers</a>
                <a href="${pageContext.request.contextPath}/ManageCommitteesServlet" id="nav-committees">🏛️ Committees</a>
                <a href="${pageContext.request.contextPath}/ManageComplaintsServlet" id="nav-complaints">📋 Complaints</a>
                <a href="${pageContext.request.contextPath}/IssueNoticeServlet" id="nav-notices">📢 Issue Notice</a>
                <a href="${pageContext.request.contextPath}/MaintenanceRecordsServlet" id="nav-maintenance">💰 Maintenance</a>
            </div>
            <div class="sidebar-footer">
                <a href="${pageContext.request.contextPath}/LogoutServlet" id="nav-logout">🚪 Logout</a>
            </div>
        </nav>

        <!-- Main Content -->
        <main class="main-content">
            <div class="page-header">
                <div>
                    <h1>Dashboard</h1>
                    <p>Overview of <c:out value="${sessionScope.societyName}"/></p>
                </div>
                <button class="btn btn-secondary btn-sm" onclick="updateDashboard()" id="btn-refresh">↻ Refresh</button>
            </div>

            <!-- Stats Grid -->
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-label">Occupied Apartments</div>
                    <div class="stat-value" id="stat-residents">—</div>
                    <div class="stat-sub">of <span id="stat-total-apts">—</span> total</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Active Complaints</div>
                    <div class="stat-value" id="stat-complaints" style="color:var(--warning);">—</div>
                    <div class="stat-sub">pending resolution</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Vehicles Registered</div>
                    <div class="stat-value" id="stat-vehicles" style="color:var(--accent);">—</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Active Helpers</div>
                    <div class="stat-value" id="stat-helpers" style="color:var(--success);">—</div>
                </div>
            </div>

            <!-- Recent Notices -->
            <div class="card">
                <div class="card-header">
                    <h2>📢 Recent Notices</h2>
                    <a href="${pageContext.request.contextPath}/IssueNoticeServlet" class="btn btn-primary btn-sm">+ Issue Notice</a>
                </div>
                <div class="card-body">
                    <div id="notices-list">
                        <p style="color:var(--text-muted);">Loading...</p>
                    </div>
                </div>
            </div>

            <!-- Committees -->
            <div class="card">
                <div class="card-header">
                    <h2>🏛️ Active Committees</h2>
                    <a href="${pageContext.request.contextPath}/ManageCommitteesServlet" class="btn btn-primary btn-sm">Manage</a>
                </div>
                <div class="card-body">
                    <div id="committees-list">
                        <p style="color:var(--text-muted);">Loading...</p>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <script>
        function updateDashboard() {
            fetch('${pageContext.request.contextPath}/adminDashboardData')
                .then(r => r.json())
                .then(data => {
                    document.getElementById('stat-residents').textContent = data.totalResidents || 0;
                    document.getElementById('stat-total-apts').textContent = data.totalApartments || 0;
                    document.getElementById('stat-complaints').textContent = data.activeComplaints || 0;
                    document.getElementById('stat-vehicles').textContent = data.totalVehicles || 0;
                    document.getElementById('stat-helpers').textContent = data.totalHelpers || 0;

                    // Notices
                    const nl = document.getElementById('notices-list');
                    if (data.notices && data.notices.length > 0) {
                        nl.innerHTML = data.notices.map(n =>
                            '<div style="padding:0.75rem 0;border-bottom:1px solid var(--border);">' +
                            '<div style="display:flex;justify-content:space-between;align-items:center;">' +
                            '<strong>' + escapeHtml(n.title) + '</strong>' +
                            '<span class="badge badge-info">' + escapeHtml(n.date) + '</span></div>' +
                            '<p style="font-size:0.85rem;color:var(--text-secondary);margin-top:0.25rem;">' +
                            escapeHtml(n.content ? n.content.substring(0, 100) : '') + '</p></div>'
                        ).join('');
                    } else {
                        nl.innerHTML = '<p style="color:var(--text-muted);">No notices yet. Issue your first notice!</p>';
                    }

                    // Committees
                    const cl = document.getElementById('committees-list');
                    if (data.committees && data.committees.length > 0) {
                        cl.innerHTML = data.committees.map(c =>
                            '<div style="padding:0.5rem 0;border-bottom:1px solid var(--border);">' +
                            '<strong>' + escapeHtml(c.name) + '</strong>' +
                            '<p style="font-size:0.85rem;color:var(--text-secondary);">' + escapeHtml(c.description || '') + '</p></div>'
                        ).join('');
                    } else {
                        cl.innerHTML = '<p style="color:var(--text-muted);">No committees created yet.</p>';
                    }
                })
                .catch(err => {
                    console.error('Dashboard update failed:', err);
                });
        }

        function escapeHtml(text) {
            if (!text) return '';
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }

        // Initial load + refresh every 5 seconds for live data
        updateDashboard();
        setInterval(updateDashboard, 5000);
    </script>
</body>
</html>
