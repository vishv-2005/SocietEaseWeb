<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Issue Notice — SocietEase</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
    <div class="app-layout">
        <nav class="sidebar">
            <div class="sidebar-logo">Societ<span>Ease</span></div>
            <div class="sidebar-society"><c:out value="${sessionScope.societyName}"/></div>
            <div class="sidebar-nav">
                <a href="${pageContext.request.contextPath}/adminDashboardData?view=dashboard">📊 Dashboard</a>
                <a href="${pageContext.request.contextPath}/ManageResidentServlet">🏠 Manage Residents</a>
                <a href="${pageContext.request.contextPath}/ManageHelperServlet">👷 Manage Helpers</a>
                <a href="${pageContext.request.contextPath}/ManageCommitteesServlet">🏛️ Committees</a>
                <a href="${pageContext.request.contextPath}/ManageComplaintsServlet">📋 Complaints</a>
                <a href="${pageContext.request.contextPath}/IssueNoticeServlet" class="active">📢 Issue Notice</a>
                <a href="${pageContext.request.contextPath}/MaintenanceRecordsServlet">💰 Maintenance</a>
            </div>
            <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/LogoutServlet">🚪 Logout</a></div>
        </nav>

        <main class="main-content">
            <div class="page-header">
                <div><h1>Issue Notice</h1><p>Broadcast notices to all residents</p></div>
            </div>

            <c:if test="${not empty sessionScope.message}">
                <div class="alert alert-${sessionScope.messageType == 'success' ? 'success' : 'error'}">
                    <c:out value="${sessionScope.message}"/>
                </div>
                <c:remove var="message" scope="session"/>
                <c:remove var="messageType" scope="session"/>
            </c:if>

            <div class="card" style="max-width:650px;">
                <div class="card-header"><h2>New Notice</h2></div>
                <div class="card-body">
                    <form method="POST" action="${pageContext.request.contextPath}/IssueNoticeServlet">
                        <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}">

                        <div class="form-group">
                            <label for="title">Notice Title *</label>
                            <input type="text" class="form-control" id="title" name="title"
                                   placeholder="e.g., Water Supply Interruption" required>
                        </div>

                        <div class="form-group">
                            <label for="committeeId">Issuing Committee *</label>
                            <select class="form-control" id="committeeId" name="committeeId" required>
                                <option value="">Select committee...</option>
                                <c:forEach var="c" items="${committees}">
                                    <option value="${c.committeeId}"><c:out value="${c.name}"/></option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="noticeDate">Date *</label>
                            <input type="date" class="form-control" id="noticeDate" name="noticeDate"
                                   value="${currentDate}" required>
                        </div>

                        <div class="form-group">
                            <label for="content">Notice Content *</label>
                            <textarea class="form-control" id="content" name="content"
                                      placeholder="Detailed notice content..." required style="min-height:150px;"></textarea>
                        </div>

                        <button type="submit" class="btn btn-primary btn-lg" style="width:100%;">📢 Publish Notice</button>
                    </form>
                </div>
            </div>
        </main>
    </div>
</body>
</html>
