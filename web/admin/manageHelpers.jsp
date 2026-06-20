<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Helpers — SocietEase</title>
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
                <a href="${pageContext.request.contextPath}/ManageHelperServlet" class="active">👷 Manage Helpers</a>
                <a href="${pageContext.request.contextPath}/ManageCommitteesServlet">🏛️ Committees</a>
                <a href="${pageContext.request.contextPath}/ManageComplaintsServlet">📋 Complaints</a>
                <a href="${pageContext.request.contextPath}/IssueNoticeServlet">📢 Issue Notice</a>
                <a href="${pageContext.request.contextPath}/MaintenanceRecordsServlet">💰 Maintenance</a>
            </div>
            <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/LogoutServlet">🚪 Logout</a></div>
        </nav>

        <main class="main-content">
            <div class="page-header">
                <div><h1>Manage Helpers</h1><p>Society staff and apartment assignments</p></div>
                <button class="btn btn-primary" onclick="document.getElementById('addHelperModal').classList.add('active')" id="btn-add-helper">+ Add Helper</button>
            </div>

            <c:if test="${not empty sessionScope.errorMessage}">
                <div class="alert alert-error">⚠️ <c:out value="${sessionScope.errorMessage}"/></div>
                <c:remove var="errorMessage" scope="session"/>
            </c:if>

            <div class="helper-grid">
                <c:forEach var="h" items="${helpers}">
                    <div class="helper-card">
                        <h3><c:out value="${h.name}"/></h3>
                        <div class="helper-role"><c:out value="${h.role}"/></div>
                        <div class="helper-detail">📞 <c:out value="${h.phone}" default="Not provided"/></div>
                        <div class="helper-detail">💰 ₹<c:out value="${h.salary}"/>/month</div>

                        <div class="apt-tags">
                            <c:forEach var="apt" items="${h.apartments}">
                                <span class="apt-tag">
                                    <c:out value="${apt.label}"/>
                                    <form method="POST" style="display:inline;margin:0;">
                                        <input type="hidden" name="action" value="unassign">
                                        <input type="hidden" name="helperId" value="${h.helperId}">
                                        <input type="hidden" name="apartmentId" value="${apt.apartmentId}">
                                        <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}">
                                        <button type="submit" style="background:none;border:none;cursor:pointer;color:var(--danger);font-size:0.7rem;" title="Unassign">✕</button>
                                    </form>
                                </span>
                            </c:forEach>
                        </div>

                        <div style="display:flex; gap:0.5rem; margin-top:0.75rem;">
                            <form method="POST" style="display:flex; gap:0.5rem; flex:1;">
                                <input type="hidden" name="action" value="assign">
                                <input type="hidden" name="helperId" value="${h.helperId}">
                                <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}">
                                <select name="apartmentId" class="form-control" style="font-size:0.8rem;padding:0.35rem;" required>
                                    <option value="">Assign apt...</option>
                                    <c:forEach var="apt" items="${apartments}">
                                        <option value="${apt.apartmentId}"><c:out value="${apt.label}"/></option>
                                    </c:forEach>
                                </select>
                                <button type="submit" class="btn btn-success btn-sm">+</button>
                            </form>
                            <form method="POST" style="display:inline;">
                                <input type="hidden" name="action" value="remove">
                                <input type="hidden" name="helperId" value="${h.helperId}">
                                <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}">
                                <button type="submit" class="btn btn-danger btn-sm" onclick="return confirm('Remove this helper?')">Remove</button>
                            </form>
                        </div>
                    </div>
                </c:forEach>

                <c:if test="${empty helpers}">
                    <p style="color:var(--text-muted); grid-column:1/-1; text-align:center; padding:2rem;">No helpers added yet. Click "+ Add Helper" to get started.</p>
                </c:if>
            </div>
        </main>
    </div>

    <!-- Add Helper Modal -->
    <div class="modal-overlay" id="addHelperModal">
        <div class="modal">
            <div class="modal-header">
                <h2>Add Helper</h2>
                <button class="modal-close" onclick="document.getElementById('addHelperModal').classList.remove('active')">&times;</button>
            </div>
            <form method="POST">
                <input type="hidden" name="action" value="add">
                <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}">
                <div class="form-group"><label>Name *</label><input type="text" class="form-control" name="name" required></div>
                <div class="form-group"><label>Role *</label>
                    <select class="form-control" name="role" required>
                        <option value="">Select...</option>
                        <option value="Security Guard">Security Guard</option>
                        <option value="Housekeeping">Housekeeping</option>
                        <option value="Plumber">Plumber</option>
                        <option value="Electrician">Electrician</option>
                        <option value="Gardener">Gardener</option>
                        <option value="Driver">Driver</option>
                        <option value="Other">Other</option>
                    </select>
                </div>
                <div class="form-row">
                    <div class="form-group"><label>Phone</label><input type="tel" class="form-control" name="phone"></div>
                    <div class="form-group"><label>Aadhar No.</label><input type="text" class="form-control" name="aadhar" maxlength="12"></div>
                </div>
                <div class="form-group"><label>Monthly Salary (₹) *</label><input type="number" class="form-control" name="salary" required min="0"></div>
                <button type="submit" class="btn btn-primary btn-lg" style="width:100%;">Add Helper</button>
            </form>
        </div>
    </div>
</body>
</html>
