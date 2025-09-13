<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Objects" %>
<%
    if (request.getAttribute("helpers") == null || request.getAttribute("apartments") == null) {
        response.sendRedirect("ManageHelperServlet");
        return;
    }
    List<Map<String, Object>> helpers = (List<Map<String, Object>>) request.getAttribute("helpers");
    List<Integer> apartments = (List<Integer>) request.getAttribute("apartments");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Manage Helpers</title>
    <style>
        body { font-family: 'Segoe UI', sans-serif; background: #f7f7fc; margin: 0; }
        .container { margin: 40px auto; max-width: 1100px; background: #fff; border-radius: 10px; box-shadow: 0 0 12px #0002; padding: 32px; }
        .return-btn { display: inline-block; margin-bottom: 18px; background: #2d3eaf; color: #fff; padding: 8px 18px; border-radius: 5px; text-decoration: none; font-size: 15px; font-weight: 500; }
        .return-btn:hover { background: #1f2e90; }
        h1 { text-align: center; color: #2d3eaf; margin-bottom: 30px; }
        .section-title { color: #2d3eaf; font-size: 20px; margin-bottom: 12px; border-left: 4px solid #2d3eaf; padding-left: 10px; }
        .add-form { display: flex; flex-wrap: wrap; gap: 16px; margin-bottom: 32px; align-items: flex-end; background: #f7f7fc; border-radius: 8px; padding: 18px 16px; box-shadow: 0 0 4px #0001; }
        .add-form input, .add-form select { padding: 8px 10px; border-radius: 5px; border: 1px solid #ccc; font-size: 15px; }
        .add-form label { font-weight: 500; color: #2d3eaf; margin-right: 4px; }
        .add-form .btn { background: #2d3eaf; color: #fff; border: none; padding: 8px 22px; border-radius: 5px; font-size: 15px; cursor: pointer; margin-left: 10px; }
        .add-form .btn:hover { background: #1f2e90; }
        .helper-list { display: flex; flex-wrap: wrap; gap: 24px; justify-content: flex-start; }
        .helper-card { background: #f9faff; border-radius: 10px; box-shadow: 0 2px 8px #0001; padding: 22px 24px; min-width: 320px; max-width: 350px; flex: 1 1 320px; display: flex; flex-direction: column; position: relative; }
        .helper-header { font-size: 18px; font-weight: 600; color: #2d3eaf; margin-bottom: 6px; }
        .helper-role { font-size: 14px; color: #555; margin-bottom: 10px; }
        .helper-info { font-size: 14px; color: #333; margin-bottom: 6px; }
        .apartment-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 10px; }
        .apt-tag { background: #2d3eaf; color: #fff; border-radius: 4px; padding: 3px 10px; font-size: 13px; display: flex; align-items: center; }
        .apt-tag .unassign-btn { background: #f5365c; color: #fff; border: none; border-radius: 3px; margin-left: 6px; font-size: 12px; padding: 2px 7px; cursor: pointer; }
        .apt-tag .unassign-btn:hover { background: #d42c4b; }
        .assign-form { display: flex; gap: 8px; margin-bottom: 10px; }
        .assign-form select { min-width: 80px; }
        .helper-actions { display: flex; gap: 10px; margin-top: 8px; }
        .remove-btn { background: #f5365c; color: #fff; border: none; border-radius: 5px; padding: 7px 16px; font-size: 14px; cursor: pointer; }
        .remove-btn:hover { background: #d42c4b; }
        @media (max-width: 900px) {
            .helper-list { flex-direction: column; gap: 18px; }
            .helper-card { max-width: 100%; min-width: 0; }
        }
    </style>
</head>
<body>
<div class="container">
    <a class="return-btn" href="adminDashboard.jsp">&larr; Return to Dashboard</a>
    <h1>Manage Helpers</h1>
    <div class="section-title">Add Helper</div>
    <form class="add-form" method="post" action="ManageHelperServlet">
        <input type="hidden" name="action" value="add">
        <label>Name: <input type="text" name="name" required></label>
        <label>Role: <input type="text" name="role" required></label>
        <label>Aadhar: <input type="text" name="aadharNumber" required></label>
        <label>Contact: <input type="text" name="contactInformation" required></label>
        <label>Salary: <input type="number" name="salary" step="0.01" required></label>
        <button class="btn" type="submit">Add Helper</button>
    </form>
    <div class="section-title">All Helpers</div>
    <div class="helper-list">
        <% if (helpers != null) for (Map<String, Object> helper : helpers) { %>
            <div class="helper-card">
                <div class="helper-header"><%= helper.get("name") %></div>
                <div class="helper-role">Role: <%= helper.get("role") %></div>
                <div class="helper-info">Aadhar: <%= helper.get("aadharNumber") %></div>
                <div class="helper-info">Contact: <%= helper.get("contactInformation") %></div>
                <div class="helper-info">Salary: â¹<%= helper.get("salary") %></div>
                <div class="apartment-tags">
                    <% List<Integer> assignedApts = (List<Integer>) helper.get("apartments");
                       if (assignedApts != null && !assignedApts.isEmpty()) {
                           for (Integer apt : assignedApts) { %>
                        <span class="apt-tag">
                            Apt <%= apt %>
                            <form method="post" action="ManageHelperServlet" style="display:inline;">
                                <input type="hidden" name="action" value="unassign">
                                <input type="hidden" name="helperID" value="<%= helper.get("helperID") %>">
                                <input type="hidden" name="apartmentNumber" value="<%= apt %>">
                                <button class="unassign-btn" type="submit">Unassign</button>
                            </form>
                        </span>
                    <%   }
                       } else { %>
                        <span style="color:#888;">Not assigned</span>
                    <% } %>
                </div>
                <form class="assign-form" method="post" action="ManageHelperServlet">
                    <input type="hidden" name="action" value="assign">
                    <input type="hidden" name="helperID" value="<%= helper.get("helperID") %>">
                    <select name="apartmentNumber" required>
                        <option value="" disabled selected>Assign to apartment...</option>
                        <% for (Integer apt : apartments) {
                            if (assignedApts == null || !assignedApts.contains(apt)) { %>
                                <option value="<%= apt %>">Apt <%= apt %></option>
                        <%   }
                        } %>
                    </select>
                    <button class="btn" type="submit">Assign</button>
                </form>
                <div class="helper-actions">
                    <form method="post" action="ManageHelperServlet" onsubmit="return confirm('Remove this helper?');">
                        <input type="hidden" name="action" value="remove">
                        <input type="hidden" name="helperID" value="<%= helper.get("helperID") %>">
                        <button class="remove-btn" type="submit">Remove</button>
                    </form>
                </div>
            </div>
        <% } %>
    </div>
</div>
</body>
</html> 