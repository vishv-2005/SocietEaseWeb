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
                <a href="${pageContext.request.contextPath}/resident/vehicleRegistration.jsp">🚗 My Vehicles</a>
            </div>
            <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/LogoutServlet">🚪 Logout</a></div>
        </nav>

        <main class="main-content">
            <div class="page-header"><div><h1>Payment History</h1><p>Your maintenance payment records</p></div></div>

            <div class="card">
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table" id="payment-table">
                            <thead>
                                <tr>
                                    <th>MONTH</th>
                                    <th>AMOUNT</th>
                                    <th>STATUS</th>
                                    <th>MODE</th>
                                    <th>PAYMENT DATE</th>
                                    <th>RAZORPAY ID</th>
                                </tr>
                            </thead>
                            <tbody id="payment-body">
                                <tr><td colspan="6" style="text-align:center;color:var(--text-muted);padding:2rem;">Loading...</td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <script>
        fetch('${pageContext.request.contextPath}/ResidentDataServlet?type=payments')
            .then(r => r.json())
            .then(data => {
                const tbody = document.getElementById('payment-body');
                if (data.payments && data.payments.length > 0) {
                    tbody.innerHTML = data.payments.map(p => {
                        let statusClass = p.status === 'PAID' ? 'badge-success' :
                                         p.status === 'FAILED' ? 'badge-error' : 'badge-warning';
                        return '<tr>' +
                            '<td>' + escapeHtml(p.month) + '</td>' +
                            '<td>₹' + p.amount + '</td>' +
                            '<td><span class="badge ' + statusClass + '">' + escapeHtml(p.status) + '</span></td>' +
                            '<td>' + escapeHtml(p.mode || '-') + '</td>' +
                            '<td>' + escapeHtml(p.date || '-') + '</td>' +
                            '<td style="font-size:0.8rem;">' + escapeHtml(p.razorpayId || '-') + '</td>' +
                            '</tr>';
                    }).join('');
                } else {
                    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:var(--text-muted);padding:2rem;">' +
                        'No payment records yet. Make your first maintenance payment!</td></tr>';
                }
            })
            .catch(() => {
                document.getElementById('payment-body').innerHTML =
                    '<tr><td colspan="6" style="text-align:center;color:var(--text-muted);padding:2rem;">Could not load payment history.</td></tr>';
            });

        function escapeHtml(t) { if(!t) return ''; const d=document.createElement('div'); d.textContent=t; return d.innerHTML; }
    </script>
</body>
</html>
