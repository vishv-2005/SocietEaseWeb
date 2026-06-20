<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Complaints — SocietEase</title>
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
                <a href="${pageContext.request.contextPath}/ManageComplaintsServlet" class="active">📋 Complaints</a>
                <a href="${pageContext.request.contextPath}/IssueNoticeServlet">📢 Issue Notice</a>
                <a href="${pageContext.request.contextPath}/MaintenanceRecordsServlet">💰 Maintenance</a>
            </div>
            <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/LogoutServlet">🚪 Logout</a></div>
        </nav>

        <main class="main-content">
            <div class="page-header">
                <div><h1>Complaints</h1><p>Track and resolve resident complaints</p></div>
            </div>

            <c:if test="${not empty sessionScope.errorMessage}">
                <div class="alert alert-error">⚠️ <c:out value="${sessionScope.errorMessage}"/></div>
                <c:remove var="errorMessage" scope="session"/>
            </c:if>

            <div style="display:flex; gap:0.5rem; margin-bottom:1.5rem;">
                <button class="btn btn-primary btn-sm" onclick="filterComplaints('all')">All</button>
                <button class="btn btn-secondary btn-sm" onclick="filterComplaints('PENDING')">Pending</button>
                <button class="btn btn-secondary btn-sm" onclick="filterComplaints('IN_PROGRESS')">In Progress</button>
                <button class="btn btn-secondary btn-sm" onclick="filterComplaints('RESOLVED')">Resolved</button>
            </div>

            <div class="card">
                <div class="card-body table-wrapper">
                    <table id="complaintsTable">
                        <thead>
                            <tr><th>Apartment</th><th>Description</th><th>Date Filed</th><th>Status</th><th>Resolved</th><th>Actions</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="c" items="${complaints}">
                                <tr data-status="${c.status}">
                                    <td><strong><c:out value="${c.apartmentLabel}"/></strong></td>
                                    <td style="max-width:300px;"><c:out value="${c.description}"/></td>
                                    <td><c:out value="${c.dateFiled}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${c.status == 'PENDING'}"><span class="badge badge-pending">Pending</span></c:when>
                                            <c:when test="${c.status == 'IN_PROGRESS'}"><span class="badge badge-info">In Progress</span></c:when>
                                            <c:when test="${c.status == 'RESOLVED'}"><span class="badge badge-success">Resolved</span></c:when>
                                        </c:choose>
                                    </td>
                                    <td><c:out value="${c.resolvedDate}" default="—"/></td>
                                    <td>
                                        <c:if test="${c.status == 'PENDING'}">
                                            <form method="POST" style="display:inline;">
                                                <input type="hidden" name="action" value="in_progress">
                                                <input type="hidden" name="complaintId" value="${c.complaintId}">
                                                <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}">
                                                <button type="submit" class="btn btn-secondary btn-sm">Start</button>
                                            </form>
                                        </c:if>
                                        <c:if test="${c.status != 'RESOLVED'}">
                                            <form method="POST" style="display:inline;">
                                                <input type="hidden" name="action" value="resolve">
                                                <input type="hidden" name="complaintId" value="${c.complaintId}">
                                                <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}">
                                                <button type="submit" class="btn btn-success btn-sm">✓ Resolve</button>
                                            </form>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty complaints}">
                                <tr><td colspan="6" style="text-align:center;color:var(--text-muted);padding:2rem;">No complaints filed.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>
    <script>
        function filterComplaints(status) {
            document.querySelectorAll('#complaintsTable tbody tr').forEach(row => {
                row.style.display = (status === 'all' || row.dataset.status === status) ? '' : 'none';
            });
        }
    </script>
</body>
</html>
