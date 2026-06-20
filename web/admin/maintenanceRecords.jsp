<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Maintenance Records — SocietEase</title>
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
                <a href="${pageContext.request.contextPath}/IssueNoticeServlet">📢 Issue Notice</a>
                <a href="${pageContext.request.contextPath}/MaintenanceRecordsServlet" class="active">💰 Maintenance</a>
            </div>
            <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/LogoutServlet">🚪 Logout</a></div>
        </nav>

        <main class="main-content">
            <div class="page-header">
                <div><h1>Maintenance Records</h1><p>Track payment status for all apartments</p></div>
            </div>

            <div style="display:flex; gap:0.5rem; margin-bottom:1.5rem;">
                <button class="btn btn-primary btn-sm" onclick="filterPayments('all')">All</button>
                <button class="btn btn-secondary btn-sm" onclick="filterPayments('PAID')">Paid</button>
                <button class="btn btn-secondary btn-sm" onclick="filterPayments('PENDING')">Pending</button>
                <button class="btn btn-secondary btn-sm" onclick="filterPayments('FAILED')">Failed</button>
            </div>

            <div class="card">
                <div class="card-body table-wrapper">
                    <table id="paymentTable">
                        <thead>
                            <tr><th>Apartment</th><th>Month</th><th>Amount</th><th>Status</th><th>Mode</th><th>Payment Date</th><th>Razorpay ID</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="r" items="${records}">
                                <tr data-status="${r.status}">
                                    <td><strong><c:out value="${r.apartmentLabel}"/></strong></td>
                                    <td><c:out value="${r.month}"/></td>
                                    <td>₹<c:out value="${r.amount}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${r.status == 'PAID'}"><span class="badge badge-paid">Paid</span></c:when>
                                            <c:when test="${r.status == 'PENDING'}"><span class="badge badge-pending">Pending</span></c:when>
                                            <c:when test="${r.status == 'FAILED'}"><span class="badge badge-failed">Failed</span></c:when>
                                        </c:choose>
                                    </td>
                                    <td><c:out value="${r.mode}" default="—"/></td>
                                    <td><c:out value="${r.paymentDate}" default="—"/></td>
                                    <td style="font-size:0.8rem;"><c:out value="${r.razorpayId}" default="—"/></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty records}">
                                <tr><td colspan="7" style="text-align:center;color:var(--text-muted);padding:2rem;">No payment records yet.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>
    <script>
        function filterPayments(status) {
            document.querySelectorAll('#paymentTable tbody tr').forEach(row => {
                row.style.display = (status === 'all' || row.dataset.status === status) ? '' : 'none';
            });
        }
    </script>
</body>
</html>
