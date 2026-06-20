<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Resident Dashboard — SocietEase</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
    <div class="app-layout">
        <nav class="sidebar">
            <div class="sidebar-logo">Societ<span>Ease</span></div>
            <div class="sidebar-society"><c:out value="${sessionScope.societyName}"/></div>
            <div class="sidebar-nav">
                <a href="${pageContext.request.contextPath}/resident/dashboard.jsp" class="active">📊 Dashboard</a>
                <a href="${pageContext.request.contextPath}/resident/notices.jsp">📢 Notices</a>
                <a href="${pageContext.request.contextPath}/resident/fileComplaint.jsp">📋 File Complaint</a>
                <a href="${pageContext.request.contextPath}/resident/payMaintenance.jsp">💳 Pay Maintenance</a>
                <a href="${pageContext.request.contextPath}/resident/paymentHistory.jsp">📄 Payment History</a>
            </div>
            <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/LogoutServlet">🚪 Logout</a></div>
        </nav>

        <main class="main-content">
            <div class="page-header">
                <div>
                    <h1>Welcome, <c:out value="${sessionScope.userName}"/>!</h1>
                    <p>Resident Dashboard — <c:out value="${sessionScope.societyName}"/></p>
                </div>
            </div>

            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-label">Your Society</div>
                    <div class="stat-value" style="font-size:1.3rem;"><c:out value="${sessionScope.societyName}"/></div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Quick Actions</div>
                    <div style="margin-top:0.75rem; display:flex; flex-direction:column; gap:0.5rem;">
                        <a href="${pageContext.request.contextPath}/resident/payMaintenance.jsp" class="btn btn-primary btn-sm">💳 Pay Maintenance</a>
                        <a href="${pageContext.request.contextPath}/resident/fileComplaint.jsp" class="btn btn-secondary btn-sm">📋 File Complaint</a>
                    </div>
                </div>
            </div>

            <div class="card">
                <div class="card-header"><h2>📢 Recent Notices</h2></div>
                <div class="card-body" id="resident-notices">
                    <p style="color:var(--text-muted);">Loading notices...</p>
                </div>
            </div>
        </main>
    </div>

    <script>
        fetch('${pageContext.request.contextPath}/adminDashboardData')
            .then(r => r.json())
            .then(data => {
                const nl = document.getElementById('resident-notices');
                if (data.notices && data.notices.length > 0) {
                    nl.innerHTML = data.notices.map(n =>
                        '<div style="padding:0.75rem 0;border-bottom:1px solid var(--border);">' +
                        '<div style="display:flex;justify-content:space-between;align-items:center;">' +
                        '<strong>' + escapeHtml(n.title) + '</strong>' +
                        '<span class="badge badge-info">' + escapeHtml(n.date) + '</span></div>' +
                        '<p style="font-size:0.85rem;color:var(--text-secondary);margin-top:0.25rem;">' +
                        escapeHtml(n.content || '') + '</p></div>'
                    ).join('');
                } else {
                    nl.innerHTML = '<p style="color:var(--text-muted);">No notices yet.</p>';
                }
            }).catch(() => {
                document.getElementById('resident-notices').innerHTML =
                    '<p style="color:var(--text-muted);">Could not load notices.</p>';
            });

        function escapeHtml(t) { if(!t) return ''; const d=document.createElement('div'); d.textContent=t; return d.innerHTML; }
    </script>
</body>
</html>
