<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    if (request.getAttribute("residents") == null) {
        response.sendRedirect("PayMaintenanceServlet");
        return;
    }
    List<Map<String, Object>> residents = (List<Map<String, Object>>) request.getAttribute("residents");
%>
<!DOCTYPE html>
<html>
<head>
    <title>SocietEase - Pay Maintenance</title>
    <style>
        body { background: #f7f7fc; font-family: 'Segoe UI', sans-serif; }
        .container { max-width: 500px; margin: 40px auto; background: #fff; border-radius: 10px; box-shadow: 0 0 12px #0002; padding: 32px; }
        h1 { text-align: center; color: #2d3eaf; margin-bottom: 30px; }
        .form-group { margin-bottom: 18px; }
        .form-group label { display: block; margin-bottom: 6px; color: #222; font-weight: 500; }
        .form-group input, .form-group select { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; font-size: 15px; }
        .form-group input[readonly] { background: #f0f0f0; }
        .btn { width: 100%; background: #2d3eaf; color: #fff; border: none; padding: 12px; border-radius: 5px; font-size: 17px; font-weight: 600; cursor: pointer; }
        .btn:hover { background: #1f2e90; }
    </style>
    <script>
        let residents = [
<% for (int i = 0; i < residents.size(); i++) { Map<String, Object> r = residents.get(i); %>
    {
        name: "<%= r.get("name") %>",
        apartment: "<%= r.get("apartmentNumber") %>",
        contact: "<%= r.get("contactInformation") %>"
    }<%= (i < residents.size() - 1) ? "," : "" %>
<% } %>
        ];
        function fillResidentDetails() {
            const select = document.getElementById('resident');
            const idx = select.selectedIndex - 1;
            if (idx >= 0) {
                document.getElementById('apartment').value = residents[idx].apartment;
                document.getElementById('contact').value = residents[idx].contact;
            } else {
                document.getElementById('apartment').value = '';
                document.getElementById('contact').value = '';
            }
        }
        window.onload = function() {
            fillResidentDetails();
        }
    </script>
</head>
<body>
    <div class="container">
        <a href="residentDashboard.jsp" style="display:inline-block;margin-bottom:18px;background:#2d3eaf;color:#fff;padding:8px 18px;border-radius:5px;text-decoration:none;font-size:15px;font-weight:500;">&larr; Back to Dashboard</a>
        <h1>Pay Maintenance</h1>
        <form method="post" action="payMaintenance">
            <div class="form-group">
                <label for="resident">Select Resident:</label>
                <select id="resident" name="resident" onchange="fillResidentDetails()" required>
                    <option value="">-- Select --</option>
                    <% for (Map<String, Object> r : residents) { %>
                        <option value="<%= r.get("apartmentNumber") %>"><%= r.get("name") %></option>
                    <% } %>
                </select>
            </div>
            <div class="form-group">
                <label for="apartment">Apartment Number:</label>
                <input type="text" id="apartment" name="apartment" readonly>
            </div>
            <div class="form-group">
                <label for="amount">Amount Paid:</label>
                <input type="number" id="amount" name="amount" required>
            </div>
            <div class="form-group">
                <label for="mode">Mode of Payment:</label>
                <select id="mode" name="mode" required>
                    <option value="Cash">Cash</option>
                    <option value="Online">Online</option>
                    <option value="Cheque">Cheque</option>
                </select>
            </div>
            <div class="form-group">
                <label for="date">Payment Date (YYYY-MM-DD):</label>
                <input type="date" id="date" name="date" value="<%= new java.sql.Date(System.currentTimeMillis()) %>" readonly>
            </div>
            <div class="form-group">
                <label for="contact">Contact Info:</label>
                <input type="text" id="contact" name="contact" readonly>
            </div>
            <button type="submit" class="btn">Submit Payment</button>
        </form>
    </div>
</body>
</html> 