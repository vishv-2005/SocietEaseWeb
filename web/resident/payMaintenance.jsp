<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pay Maintenance — SocietEase</title>
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
                <a href="${pageContext.request.contextPath}/resident/payMaintenance.jsp" class="active">💳 Pay Maintenance</a>
                <a href="${pageContext.request.contextPath}/resident/paymentHistory.jsp">📄 Payment History</a>
                <a href="${pageContext.request.contextPath}/resident/vehicleRegistration.jsp">🚗 My Vehicles</a>
            </div>
            <div class="sidebar-footer"><a href="${pageContext.request.contextPath}/LogoutServlet">🚪 Logout</a></div>
        </nav>

        <main class="main-content">
            <div class="page-header">
                <div>
                    <h1>Pay Maintenance</h1>
                    <p>Monthly maintenance fee: ₹<c:out value="${sessionScope.maintenanceAmount != null ? sessionScope.maintenanceAmount : 1000}"/></p>
                </div>
            </div>

            <c:if test="${empty sessionScope.apartmentId}">
                <div class="alert alert-error">
                    Your account is not linked to an apartment. Please contact your society admin to assign you to an apartment before making payments.
                </div>
            </c:if>

            <c:if test="${not empty sessionScope.apartmentId}">
            <div class="card" style="max-width:500px;">
                <div class="card-header"><h2>💳 Make Payment</h2></div>
                <div class="card-body">
                    <div id="payment-status"></div>

                    <div class="form-group">
                        <label for="paymentMonth">Payment Month *</label>
                        <input type="month" class="form-control" id="paymentMonth" required>
                    </div>

                    <div style="background:var(--primary-bg); border-radius:var(--radius-sm); padding:1.25rem; margin:1rem 0;">
                        <div style="display:flex;justify-content:space-between;align-items:center;">
                            <span style="font-weight:600;">Maintenance Amount</span>
                            <span style="font-size:1.5rem;font-weight:800;color:var(--primary);">₹<c:out value="${sessionScope.maintenanceAmount != null ? sessionScope.maintenanceAmount : 1000}"/></span>
                        </div>
                    </div>

                    <button class="btn btn-primary btn-lg" style="width:100%;" onclick="initiatePayment()" id="btn-pay">
                        Pay ₹<c:out value="${sessionScope.maintenanceAmount != null ? sessionScope.maintenanceAmount : 1000}"/> via Razorpay
                    </button>

                    <p style="text-align:center; margin-top:1rem; font-size:0.8rem; color:var(--text-muted);">
                        Powered by Razorpay. Supports UPI, Cards, NetBanking, and Wallets.
                    </p>
                </div>
            </div>
            </c:if>
        </main>
    </div>

    <script src="https://checkout.razorpay.com/v1/checkout.js"></script>
    <script>
        var maintenanceAmount = ${sessionScope.maintenanceAmount != null ? sessionScope.maintenanceAmount : 1000};

        function initiatePayment() {
            const month = document.getElementById('paymentMonth').value;
            if (!month) { alert('Please select a payment month.'); return; }

            document.getElementById('btn-pay').disabled = true;
            document.getElementById('btn-pay').textContent = 'Creating order...';

            fetch('${pageContext.request.contextPath}/CreateOrderServlet', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'month=' + encodeURIComponent(month)
            })
            .then(r => r.json())
            .then(data => {
                if (data.error) {
                    showStatus('error', data.error);
                    resetButton();
                    return;
                }

                const options = {
                    key: data.razorpayKeyId,
                    amount: data.amount,
                    currency: 'INR',
                    name: 'SocietEase',
                    description: 'Maintenance for ' + month,
                    order_id: data.orderId,
                    handler: function(response) {
                        verifyPayment(response, month);
                    },
                    prefill: {
                        email: '${sessionScope.userEmail}',
                        contact: ''
                    },
                    theme: { color: '#4f46e5' },
                    modal: {
                        ondismiss: function() { resetButton(); }
                    }
                };

                const rzp = new Razorpay(options);
                rzp.on('payment.failed', function(resp) {
                    showStatus('error', 'Payment failed: ' + resp.error.description);
                    resetButton();
                });
                rzp.open();
            })
            .catch(err => {
                console.error('Create order error:', err);
                showStatus('error', 'Failed to create payment order. Please try again.');
                resetButton();
            });
        }

        function verifyPayment(response, month) {
            document.getElementById('btn-pay').textContent = 'Verifying...';

            fetch('${pageContext.request.contextPath}/VerifyPaymentServlet', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'razorpay_payment_id=' + encodeURIComponent(response.razorpay_payment_id) +
                      '&razorpay_order_id=' + encodeURIComponent(response.razorpay_order_id) +
                      '&razorpay_signature=' + encodeURIComponent(response.razorpay_signature) +
                      '&month=' + encodeURIComponent(month)
            })
            .then(r => r.json())
            .then(data => {
                if (data.status === 'success') {
                    showStatus('success', '✅ Payment successful! Payment ID: ' + data.paymentId);
                } else {
                    showStatus('error', 'Payment verification failed: ' + (data.error || 'Unknown error'));
                }
                resetButton();
            })
            .catch(() => {
                showStatus('error', 'Verification failed. Please contact the society admin.');
                resetButton();
            });
        }

        function showStatus(type, message) {
            document.getElementById('payment-status').innerHTML =
                '<div class="alert alert-' + (type === 'success' ? 'success' : 'error') + '">' + message + '</div>';
        }

        function resetButton() {
            document.getElementById('btn-pay').disabled = false;
            document.getElementById('btn-pay').textContent = 'Pay ₹' + maintenanceAmount + ' via Razorpay';
        }

        // Default to current month
        const now = new Date();
        document.getElementById('paymentMonth').value =
            now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0');
    </script>
</body>
</html>
