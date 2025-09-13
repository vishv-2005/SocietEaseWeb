<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    if (request.getAttribute("complaints") == null) {
        response.sendRedirect("ManageComplaintsServlet");
        return;
    }
    List<Map<String, Object>> complaints = (List<Map<String, Object>>) request.getAttribute("complaints");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Manage Complaints</title>
    <style>
        body { font-family: 'Segoe UI', sans-serif; background: #f7f7fc; margin: 0; }
        .container { margin: 40px auto; max-width: 1200px; background: #fff; border-radius: 10px; box-shadow: 0 0 12px #0002; padding: 32px; }
        .return-btn { display: inline-block; margin-bottom: 18px; background: #2d3eaf; color: #fff; padding: 8px 18px; border-radius: 5px; text-decoration: none; font-size: 15px; font-weight: 500; }
        .return-btn:hover { background: #1f2e90; }
        h1 { text-align: center; color: #2d3eaf; margin-bottom: 30px; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #f8f9fa; color: #2d3eaf; font-weight: 600; }
        tr:hover { background-color: #f5f6ff; }
        .date { white-space: nowrap; }
        .status { padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 500; }
        .status.pending { background: #fff7e6; color: #fa8c16; }
        .search-box { margin-bottom: 20px; padding: 10px; background: #f8f9fa; border-radius: 5px; }
        .search-box input { padding: 8px; width: 200px; border: 1px solid #ddd; border-radius: 4px; }
        .search-box button { padding: 8px 16px; background: #2d3eaf; color: white; border: none; border-radius: 4px; cursor: pointer; }
        .search-box button:hover { background: #1f2e90; }
        .resolve-btn { background: #52c41a; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 13px; }
        .resolve-btn:hover { background: #389e0d; }
        .description { max-width: 300px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
        .description:hover { white-space: normal; overflow: visible; }
    </style>
</head>
<body>
<div class="container">
    <a class="return-btn" href="adminDashboard.jsp">&larr; Return to Dashboard</a>
    <h1>Manage Complaints</h1>
    
    <div class="search-box">
        <input type="text" id="searchInput" placeholder="Search by apartment or description...">
        <button onclick="searchComplaints()">Search</button>
    </div>

    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Apartment</th>
                <th>Resident</th>
                <th>Contact</th>
                <th>Description</th>
                <th>Date Filed</th>
                <th>Status</th>
                <th>Action</th>
            </tr>
        </thead>
        <tbody>
            <% for (Map<String, Object> complaint : complaints) { %>
                <tr>
                    <td><%= complaint.get("complaintID") %></td>
                    <td><%= complaint.get("apartmentNumber") %></td>
                    <td><%= complaint.get("residentName") %></td>
                    <td><%= complaint.get("contactInformation") %></td>
                    <td class="description"><%= complaint.get("description") %></td>
                    <td class="date"><%= complaint.get("dateFiled") %></td>
                    <td><span class="status pending"><%= complaint.get("status") %></span></td>
                    <td>
                        <form method="post" action="ManageComplaintsServlet" onsubmit="return confirm('Are you sure you want to resolve this complaint?');">
                            <input type="hidden" name="action" value="resolve">
                            <input type="hidden" name="complaintID" value="<%= complaint.get("complaintID") %>">
                            <button type="submit" class="resolve-btn">Resolve</button>
                        </form>
                    </td>
                </tr>
            <% } %>
        </tbody>
    </table>
</div>

<script>
function searchComplaints() {
    const input = document.getElementById('searchInput');
    const filter = input.value.toLowerCase();
    const table = document.querySelector('table');
    const rows = table.getElementsByTagName('tr');

    for (let i = 1; i < rows.length; i++) {
        const row = rows[i];
        const cells = row.getElementsByTagName('td');
        let found = false;

        for (let j = 0; j < cells.length; j++) {
            const cell = cells[j];
            if (cell) {
                const text = cell.textContent || cell.innerText;
                if (text.toLowerCase().indexOf(filter) > -1) {
                    found = true;
                    break;
                }
            }
        }
        row.style.display = found ? '' : 'none';
    }
}
</script>
</body>
</html> 