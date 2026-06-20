<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Residents — SocietEase</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
    <div class="app-layout">
        <nav class="sidebar">
            <div class="sidebar-logo">Societ<span>Ease</span></div>
            <div class="sidebar-society"><c:out value="${sessionScope.societyName}"/></div>
            <div class="sidebar-nav">
                <a href="${pageContext.request.contextPath}/adminDashboardData?view=dashboard">📊 Dashboard</a>
                <a href="${pageContext.request.contextPath}/ManageResidentServlet" class="active">🏠 Manage Residents</a>
                <a href="${pageContext.request.contextPath}/ManageHelperServlet">👷 Manage Helpers</a>
                <a href="${pageContext.request.contextPath}/ManageCommitteesServlet">🏛️ Committees</a>
                <a href="${pageContext.request.contextPath}/ManageComplaintsServlet">📋 Complaints</a>
                <a href="${pageContext.request.contextPath}/IssueNoticeServlet">📢 Issue Notice</a>
                <a href="${pageContext.request.contextPath}/MaintenanceRecordsServlet">💰 Maintenance</a>
            </div>
            <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/LogoutServlet">🚪 Logout</a></div>
        </nav>

        <main class="main-content">
            <div class="page-header">
                <div>
                    <h1>Manage Residents</h1>
                    <p>View and manage apartment occupancy</p>
                </div>
            </div>

            <c:if test="${not empty sessionScope.errorMessage}">
                <div class="alert alert-error">⚠️ <c:out value="${sessionScope.errorMessage}"/></div>
                <c:remove var="errorMessage" scope="session"/>
            </c:if>

            <!-- Filter Tabs -->
            <div style="display:flex; gap:0.5rem; margin-bottom:1.5rem; flex-wrap:wrap;">
                <button class="btn btn-primary btn-sm" onclick="filterTable('all')">All</button>
                <button class="btn btn-secondary btn-sm" onclick="filterTable('VACANT')">Vacant</button>
                <button class="btn btn-secondary btn-sm" onclick="filterTable('OWNER_OCCUPIED')">Owner</button>
                <button class="btn btn-secondary btn-sm" onclick="filterTable('TENANT_OCCUPIED')">Tenant</button>
            </div>

            <div class="card">
                <div class="card-body table-wrapper">
                    <table id="residentsTable">
                        <thead>
                            <tr>
                                <th>Apartment</th>
                                <th>Tower</th>
                                <th>Status</th>
                                <th>Resident Name</th>
                                <th>Phone</th>
                                <th>Email</th>
                                <th>Type</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="apt" items="${apartments}">
                                <tr data-status="${apt.status}">
                                    <td><strong><c:out value="${apt.label}"/></strong></td>
                                    <td><c:out value="${apt.towerName}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${apt.status == 'VACANT'}"><span class="badge badge-vacant">Vacant</span></c:when>
                                            <c:when test="${apt.status == 'OWNER_OCCUPIED'}"><span class="badge badge-occupied">Owner</span></c:when>
                                            <c:when test="${apt.status == 'TENANT_OCCUPIED'}"><span class="badge badge-info">Tenant</span></c:when>
                                        </c:choose>
                                    </td>
                                    <td><c:out value="${apt.name}" default="—"/></td>
                                    <td><c:out value="${apt.phone}" default="—"/></td>
                                    <td><c:out value="${apt.email}" default="—"/></td>
                                    <td><c:out value="${apt.type}" default="—"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${apt.status == 'VACANT'}">
                                                <button class="btn btn-success btn-sm" onclick="openAddModal(${apt.apartmentId}, '${apt.label}')">+ Add</button>
                                            </c:when>
                                            <c:otherwise>
                                                <button class="btn btn-secondary btn-sm" onclick="openEditModal(${apt.apartmentId}, ${apt.residentId}, '${apt.label}')">Edit</button>
                                                <form method="POST" style="display:inline;">
                                                    <input type="hidden" name="action" value="remove">
                                                    <input type="hidden" name="apartmentId" value="${apt.apartmentId}">
                                                    <input type="hidden" name="residentId" value="${apt.residentId}">
                                                    <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}">
                                                    <button type="submit" class="btn btn-danger btn-sm" onclick="return confirm('Remove this resident?')">Remove</button>
                                                </form>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>

    <!-- Add/Edit Modal -->
    <div class="modal-overlay" id="residentModal">
        <div class="modal">
            <div class="modal-header">
                <h2 id="modalTitle">Add Resident</h2>
                <button class="modal-close" onclick="closeModal()">&times;</button>
            </div>
            <form method="POST" id="residentForm">
                <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}">
                <input type="hidden" name="action" id="formAction" value="add">
                <input type="hidden" name="apartmentId" id="formApartmentId">
                <input type="hidden" name="residentId" id="formResidentId">

                <div id="modalAptLabel" style="font-weight:600; color:var(--primary); margin-bottom:1rem;"></div>

                <div class="form-group">
                    <label for="resName">Full Name *</label>
                    <input type="text" class="form-control" id="resName" name="name" required>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="resPhone">Phone *</label>
                        <input type="tel" class="form-control" id="resPhone" name="phone" required>
                    </div>
                    <div class="form-group">
                        <label for="resEmail">Email *</label>
                        <input type="email" class="form-control" id="resEmail" name="email" required>
                    </div>
                </div>
                <div class="form-group">
                    <label for="resType">Resident Type *</label>
                    <select class="form-control" id="resType" name="type" required>
                        <option value="OWNER">Owner</option>
                        <option value="TENANT">Tenant</option>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary btn-lg" style="width:100%;">Save Resident</button>
            </form>
        </div>
    </div>

    <script>
        function openAddModal(aptId, label) {
            document.getElementById('modalTitle').textContent = 'Add Resident';
            document.getElementById('formAction').value = 'add';
            document.getElementById('formApartmentId').value = aptId;
            document.getElementById('formResidentId').value = '';
            document.getElementById('modalAptLabel').textContent = 'Apartment: ' + label;
            document.getElementById('residentForm').reset();
            document.getElementById('residentModal').classList.add('active');
        }

        function openEditModal(aptId, resId, label) {
            document.getElementById('modalTitle').textContent = 'Edit Resident';
            document.getElementById('formAction').value = 'update';
            document.getElementById('formApartmentId').value = aptId;
            document.getElementById('formResidentId').value = resId;
            document.getElementById('modalAptLabel').textContent = 'Apartment: ' + label;
            document.getElementById('residentModal').classList.add('active');
        }

        function closeModal() { document.getElementById('residentModal').classList.remove('active'); }

        function filterTable(status) {
            document.querySelectorAll('#residentsTable tbody tr').forEach(row => {
                row.style.display = (status === 'all' || row.dataset.status === status) ? '' : 'none';
            });
        }
    </script>
</body>
</html>
