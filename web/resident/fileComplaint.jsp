<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>File Complaint — SocietEase</title>
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
                <a href="${pageContext.request.contextPath}/resident/fileComplaint.jsp" class="active">📋 File Complaint</a>
                <a href="${pageContext.request.contextPath}/resident/payMaintenance.jsp">💳 Pay Maintenance</a>
                <a href="${pageContext.request.contextPath}/resident/paymentHistory.jsp">📄 Payment History</a>
                <a href="${pageContext.request.contextPath}/resident/vehicleRegistration.jsp">🚗 My Vehicles</a>
            </div>
            <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/LogoutServlet">🚪 Logout</a></div>
        </nav>

        <main class="main-content">
            <div class="page-header"><div><h1>File a Complaint</h1><p>Report issues to the society management</p></div></div>

            <c:if test="${not empty sessionScope.message}">
                <div class="alert alert-${sessionScope.messageType == 'success' ? 'success' : 'error'}">
                    <c:out value="${sessionScope.message}"/>
                </div>
                <c:remove var="message" scope="session"/>
                <c:remove var="messageType" scope="session"/>
            </c:if>

            <div class="card" style="max-width:600px;">
                <div class="card-header"><h2>📋 New Complaint</h2></div>
                <div class="card-body">
                    <form method="POST" action="${pageContext.request.contextPath}/FileComplaintServlet">
                        <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}">
                        <div class="form-group">
                            <label for="description">Describe your issue in detail *</label>
                            <textarea class="form-control" id="description" name="description"
                                      placeholder="e.g., Water leakage from apartment above, water pump not working..."
                                      required style="min-height:150px;"></textarea>
                        </div>
                        <button type="submit" class="btn btn-primary btn-lg" style="width:100%;">Submit Complaint</button>
                    </form>
                </div>
            </div>
        </main>
    </div>
</body>
</html>
