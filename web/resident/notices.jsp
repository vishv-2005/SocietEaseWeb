<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Notices — SocietEase</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
    <div class="app-layout">
        <nav class="sidebar">
            <div class="sidebar-logo">Societ<span>Ease</span></div>
            <div class="sidebar-society"><c:out value="${sessionScope.societyName}"/></div>
            <div class="sidebar-nav">
                <a href="${pageContext.request.contextPath}/resident/dashboard.jsp">📊 Dashboard</a>
                <a href="${pageContext.request.contextPath}/resident/notices.jsp" class="active">📢 Notices</a>
                <a href="${pageContext.request.contextPath}/resident/fileComplaint.jsp">📋 File Complaint</a>
                <a href="${pageContext.request.contextPath}/resident/payMaintenance.jsp">💳 Pay Maintenance</a>
                <a href="${pageContext.request.contextPath}/resident/paymentHistory.jsp">📄 Payment History</a>
                <a href="${pageContext.request.contextPath}/resident/vehicleRegistration.jsp">🚗 My Vehicles</a>
            </div>
            <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/LogoutServlet">🚪 Logout</a></div>
        </nav>

        <main class="main-content">
            <div class="page-header"><div><h1>Society Notices</h1><p>All announcements from your society</p></div></div>
            <div class="card">
                <div class="card-body" id="notices-list">
                    <p style="color:var(--text-muted);">Loading notices...</p>
                </div>
            </div>
        </main>
    </div>

    <script>
        fetch('${pageContext.request.contextPath}/ResidentDataServlet?type=notices')
            .then(r => r.json())
            .then(data => {
                const nl = document.getElementById('notices-list');
                if (data.notices && data.notices.length > 0) {
                    nl.innerHTML = data.notices.map(n =>
                        '<div style="padding:1rem 0;border-bottom:1px solid var(--border);">' +
                        '<div style="display:flex;justify-content:space-between;align-items:flex-start;">' +
                        '<h3 style="font-size:1.05rem;font-weight:600;">' + escapeHtml(n.title) + '</h3>' +
                        '<span class="badge badge-info">' + escapeHtml(n.date) + '</span></div>' +
                        (n.committee ? '<div style="font-size:0.8rem;color:var(--text-muted);margin:0.25rem 0;">By: ' + escapeHtml(n.committee) + '</div>' : '') +
                        '<p style="font-size:0.9rem;color:var(--text-secondary);margin-top:0.5rem;">' +
                        escapeHtml(n.content || '') + '</p></div>'
                    ).join('');
                } else {
                    nl.innerHTML = '<p style="color:var(--text-muted);text-align:center;padding:2rem;">No notices published yet.</p>';
                }
            }).catch(() => {
                document.getElementById('notices-list').innerHTML = '<p style="color:var(--text-muted);">Could not load notices. Please try refreshing.</p>';
            });
        function escapeHtml(t) { if(!t)return''; const d=document.createElement('div'); d.textContent=t; return d.innerHTML; }
    </script>
</body>
</html>
