<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    if (request.getAttribute("vehicles") == null) {
        response.sendRedirect("ViewVehiclesServlet");
        return;
    }
    List<Map<String, Object>> vehicles = (List<Map<String, Object>>) request.getAttribute("vehicles");
%>
<!DOCTYPE html>
<html>
<head>
    <title>SocietEase - View Vehicles</title>
    <style>
        body { background: #f7f7fc; font-family: 'Segoe UI', sans-serif; }
        .container { max-width: 900px; margin: 40px auto; background: #fff; border-radius: 10px; box-shadow: 0 0 12px #0002; padding: 32px; }
        h1 { text-align: center; color: #2d3eaf; margin-bottom: 30px; }
        .back-btn { display:inline-block;margin-bottom:18px;background:#2d3eaf;color:#fff;padding:8px 18px;border-radius:5px;text-decoration:none;font-size:15px;font-weight:500; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #f8f9fa; color: #2d3eaf; font-weight: 600; }
        tr:hover { background-color: #f5f6ff; }
    </style>
</head>
<body>
    <div class="container">
        <a href="residentDashboard.jsp" class="back-btn">&larr; Back to Dashboard</a>
        <h1>All Vehicles</h1>
        <table>
            <thead>
                <tr>
                    <th>Vehicle Number</th>
                    <th>Type</th>
                    <th>Owner</th>
                    <th>Apartment Number</th>
                </tr>
            </thead>
            <tbody>
                <% for (Map<String, Object> vehicle : vehicles) { %>
                    <tr>
                        <td><%= vehicle.get("number") %></td>
                        <td><%= vehicle.get("type") %></td>
                        <td><%= vehicle.get("owner") %></td>
                        <td><%= vehicle.get("apartmentNumber") %></td>
                    </tr>
                <% } %>
            </tbody>
        </table>
    </div>
</body>
</html> 