<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Objects" %>
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%@ page import="javax.servlet.http.HttpServletResponse" %>
<%@ page import="javax.servlet.*" %>
<%
    if (request.getAttribute("residents") == null) {
        response.sendRedirect("ManageResidentServlet");
        return;
    }
    List<Map<String, Object>> residents = (List<Map<String, Object>>) request.getAttribute("residents");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Manage Residents</title>
    <style>
        body { font-family: 'Segoe UI', sans-serif; background: #f7f7fc; margin: 0; }
        .container { margin: 40px auto; max-width: 1000px; background: #fff; border-radius: 8px; box-shadow: 0 0 8px #0001; padding: 30px; }
        h1 { text-align: center; color: #2d3eaf; }
        .return-btn { display: inline-block; margin-bottom: 18px; background: #2d3eaf; color: #fff; padding: 8px 18px; border-radius: 5px; text-decoration: none; font-size: 15px; font-weight: 500; }
        .return-btn:hover { background: #1f2e90; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { padding: 10px; border: 1px solid #ccc; text-align: center; }
        th { background: #2d3eaf; color: #fff; }
        .btn { padding: 6px 14px; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
        .btn-update { background: #2d3eaf; color: #fff; }
        .btn-remove { background: #f5365c; color: #fff; }
        .btn-add { background: #28a745; color: #fff; }
        .btn:disabled { background: #ccc; cursor: not-allowed; }
        form.inline-form { display: block; margin: 0; }
        .type-select, input[type=text], input[type=email] { padding: 4px 8px; border-radius: 4px; border: 1px solid #ccc; font-size: 13px; margin-bottom: 6px; display: block; width: 100%; box-sizing: border-box; text-align: left; }
        .button-row { display: flex; flex-direction: row; gap: 8px; margin-top: 6px; justify-content: flex-start; }
    </style>
</head>
<body>
<div class="container">
    <a class="return-btn" href="adminDashboard.jsp">&larr; Return to Dashboard</a>
    <h1>Manage Residents</h1>
    <table>
        <thead>
            <tr>
                <th>Apartment Number</th>
                <th>Name</th>
                <th>Contact</th>
                <th>Email</th>
                <th>Type</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <% for (Map<String, Object> res : residents) { %>
            <tr>
                <td><%= res.get("apartmentNumber") %></td>
                <td><%= Objects.toString(res.get("name"), "") %></td>
                <td><%= Objects.toString(res.get("contactInformation"), "") %></td>
                <td><%= Objects.toString(res.get("email"), "") %></td>
                <td><%= Objects.toString(res.get("type"), "") %></td>
                <td>
                    <% if (res.get("name") != null) { %>
                        <form class="inline-form" method="post" action="ManageResidentServlet">
                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="apartmentNumber" value="<%= res.get("apartmentNumber") %>">
                            <input type="text" name="name" value="<%= res.get("name") %>" required>
                            <input type="text" name="contactInformation" value="<%= res.get("contactInformation") %>" required>
                            <input type="email" name="email" value="<%= res.get("email") %>" required>
                            <select class="type-select" name="type" required>
                                <option value="Owner" <%= "Owner".equals(res.get("type")) ? "selected" : "" %>>Owner</option>
                                <option value="Tenant" <%= "Tenant".equals(res.get("type")) ? "selected" : "" %>>Tenant</option>
                            </select>
                            <div class="button-row">
                                <button class="btn btn-update" type="submit">Update</button>
                        </form>
                        <form class="inline-form" method="post" action="ManageResidentServlet" onsubmit="return confirm('Are you sure you want to remove this resident?');">
                            <input type="hidden" name="action" value="remove">
                            <input type="hidden" name="apartmentNumber" value="<%= res.get("apartmentNumber") %>">
                            <button class="btn btn-remove" type="submit">Remove</button>
                        </form>
                            </div>
                    <% } else { %>
                        <form class="inline-form" method="post" action="ManageResidentServlet">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="apartmentNumber" value="<%= res.get("apartmentNumber") %>">
                            <input type="text" name="name" placeholder="Name" required>
                            <input type="text" name="contactInformation" placeholder="Contact" required>
                            <input type="email" name="email" placeholder="Email" required>
                            <select class="type-select" name="type" required>
                                <option value="Owner">Owner</option>
                                <option value="Tenant">Tenant</option>
                            </select>
                            <button class="btn btn-add" type="submit">Add</button>
                        </form>
                    <% } %>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
</div>
</body>
</html> 