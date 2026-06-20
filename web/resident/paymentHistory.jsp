<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Payment History — SocietEase</title>
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
                <a href="${pageContext.request.contextPath}/resident/paymentHistory.jsp" class="active">📄 Payment History</a>
            </div>
            <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/LogoutServlet">🚪 Logout</a></div>
        </nav>

        <main class="main-content">
            <div class="page-header"><div><h1>Payment History</h1><p>Your maintenance payment records</p></div></div>

            <div class="card">
                <div class="card-body" id="payment-history">
                    <p style="color:var(--text-muted);text-align:center;padding:2rem;">
                        Payment history will appear here once you make your first maintenance payment.
                    </p>
                </div>
            </div>
        </main>
    </div>
</body>
</html>
