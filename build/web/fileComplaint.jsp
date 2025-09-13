<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    if (request.getAttribute("apartments") == null) {
        response.sendRedirect("fileComplaint");
        return;
    }
    List<Map<String, Object>> apartments = (List<Map<String, Object>>) request.getAttribute("apartments");
%>
<!DOCTYPE html>
<html>
<head>
    <title>SocietEase - File Complaint</title>
    <style>
        body { background: #f7f7fc; font-family: 'Segoe UI', sans-serif; }
        .container { max-width: 500px; margin: 40px auto; background: #fff; border-radius: 10px; box-shadow: 0 0 12px #0002; padding: 32px; }
        h1 { text-align: center; color: #2d3eaf; margin-bottom: 30px; }
        .form-group { margin-bottom: 18px; }
        .form-group label { display: block; margin-bottom: 6px; color: #222; font-weight: 500; }
        .form-group input, .form-group select, .form-group textarea { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; font-size: 15px; }
        .form-group textarea { min-height: 100px; resize: vertical; }
        .btn { width: 100%; background: #2d3eaf; color: #fff; border: none; padding: 12px; border-radius: 5px; font-size: 17px; font-weight: 600; cursor: pointer; }
        .btn:hover { background: #1f2e90; }
        .back-btn { display:inline-block;margin-bottom:18px;background:#2d3eaf;color:#fff;padding:8px 18px;border-radius:5px;text-decoration:none;font-size:15px;font-weight:500; }
    </style>
</head>
<body>
    <div class="container">
        <a href="residentDashboard.jsp" class="back-btn">&larr; Back to Dashboard</a>
        <h1>File Complaint</h1>
        <form method="post" action="fileComplaint">
            <div class="form-group">
                <label for="apartment">Select Apartment:</label>
                <select id="apartment" name="apartment" required>
                    <option value="">-- Select --</option>
                    <% for (Map<String, Object> apt : apartments) { %>
                        <option value="<%= apt.get("apartmentNumber") %>">Apt <%= apt.get("apartmentNumber") %></option>
                    <% } %>
                </select>
            </div>
            <div class="form-group">
                <label for="content">Complaint Content:</label>
                <textarea id="content" name="content" required placeholder="Describe your issue..."></textarea>
            </div>
            <div class="form-group">
                <label for="date">Date:</label>
                <input type="date" id="date" name="date" value="<%= new java.sql.Date(System.currentTimeMillis()) %>" readonly>
            </div>
            <button type="submit" class="btn">Submit Complaint</button>
        </form>
    </div>
</body>
</html> 