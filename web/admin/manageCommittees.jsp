<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Committees — SocietEase</title>
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
                <a href="${pageContext.request.contextPath}/ManageCommitteesServlet" class="active">🏛️ Committees</a>
                <a href="${pageContext.request.contextPath}/ManageComplaintsServlet">📋 Complaints</a>
                <a href="${pageContext.request.contextPath}/IssueNoticeServlet">📢 Issue Notice</a>
                <a href="${pageContext.request.contextPath}/MaintenanceRecordsServlet">💰 Maintenance</a>
            </div>
            <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/LogoutServlet">🚪 Logout</a></div>
        </nav>

        <main class="main-content">
            <div class="page-header">
                <div><h1>Committees</h1><p>Manage society committees</p></div>
                <button class="btn btn-primary" onclick="openCreateModal()" id="btn-create-committee">+ Create Committee</button>
            </div>

            <c:if test="${not empty sessionScope.errorMessage}">
                <div class="alert alert-error">⚠️ <c:out value="${sessionScope.errorMessage}"/></div>
                <c:remove var="errorMessage" scope="session"/>
            </c:if>

            <div class="card">
                <div class="card-body table-wrapper">
                    <table>
                        <thead>
                            <tr><th>Name</th><th>Description</th><th>Actions</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="c" items="${committees}">
                                <tr>
                                    <td><strong><c:out value="${c.name}"/></strong></td>
                                    <td><c:out value="${c.description}" default="—"/></td>
                                    <td>
                                        <button class="btn btn-secondary btn-sm"
                                            onclick="openEditModal(${c.committeeId}, '<c:out value="${c.name}"/>', '<c:out value="${c.description}"/>')">Edit</button>
                                        <form method="POST" style="display:inline;">
                                            <input type="hidden" name="action" value="dissolve">
                                            <input type="hidden" name="committeeId" value="${c.committeeId}">
                                            <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}">
                                            <button type="submit" class="btn btn-danger btn-sm" onclick="return confirm('Dissolve this committee? Associated notices will be kept.')">Dissolve</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty committees}">
                                <tr><td colspan="3" style="text-align:center;color:var(--text-muted);padding:2rem;">No committees yet.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>

    <!-- Create/Edit Modal -->
    <div class="modal-overlay" id="committeeModal">
        <div class="modal">
            <div class="modal-header">
                <h2 id="cmModalTitle">Create Committee</h2>
                <button class="modal-close" onclick="document.getElementById('committeeModal').classList.remove('active')">&times;</button>
            </div>
            <form method="POST" id="committeeForm">
                <input type="hidden" name="action" id="cmAction" value="create">
                <input type="hidden" name="committeeId" id="cmId">
                <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}">
                <div class="form-group"><label>Committee Name *</label><input type="text" class="form-control" name="name" id="cmName" required></div>
                <div class="form-group"><label>Description</label><textarea class="form-control" name="description" id="cmDesc"></textarea></div>
                <button type="submit" class="btn btn-primary btn-lg" style="width:100%;" id="cmSubmitBtn">Create</button>
            </form>
        </div>
    </div>

    <script>
        function openCreateModal() {
            document.getElementById('cmModalTitle').textContent = 'Create Committee';
            document.getElementById('cmAction').value = 'create';
            document.getElementById('cmId').value = '';
            document.getElementById('cmName').value = '';
            document.getElementById('cmDesc').value = '';
            document.getElementById('cmSubmitBtn').textContent = 'Create';
            document.getElementById('committeeModal').classList.add('active');
        }
        function openEditModal(id, name, desc) {
            document.getElementById('cmModalTitle').textContent = 'Edit Committee';
            document.getElementById('cmAction').value = 'edit';
            document.getElementById('cmId').value = id;
            document.getElementById('cmName').value = name;
            document.getElementById('cmDesc').value = desc || '';
            document.getElementById('cmSubmitBtn').textContent = 'Save Changes';
            document.getElementById('committeeModal').classList.add('active');
        }
    </script>
</body>
</html>
