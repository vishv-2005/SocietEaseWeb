<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    if (request.getAttribute("vehicles") == null || request.getAttribute("residents") == null) {
        response.sendRedirect("ManageVehiclesServlet");
        return;
    }
    List<Map<String, Object>> vehicles = (List<Map<String, Object>>) request.getAttribute("vehicles");
    List<Map<String, Object>> residents = (List<Map<String, Object>>) request.getAttribute("residents");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Manage Vehicles</title>
    <style>
        body { 
            font-family: 'Segoe UI', sans-serif; 
            background: #f7f7fc; 
            margin: 0; 
        }
        .container { 
            margin: 40px auto; 
            max-width: 1200px; 
            background: #fff; 
            border-radius: 10px; 
            box-shadow: 0 0 12px #0002; 
            padding: 32px; 
        }
        .return-btn { 
            display: inline-block; 
            margin-bottom: 18px; 
            background: #2d3eaf; 
            color: #fff; 
            padding: 8px 18px; 
            border-radius: 5px; 
            text-decoration: none; 
            font-size: 15px; 
            font-weight: 500; 
        }
        .return-btn:hover { 
            background: #1f2e90; 
        }
        h1 { 
            text-align: center; 
            color: #2d3eaf; 
            margin-bottom: 30px; 
        }
        .add-form {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 30px;
        }
        .add-form h2 {
            color: #2d3eaf;
            margin-top: 0;
            margin-bottom: 20px;
            font-size: 18px;
        }
        .form-group {
            margin-bottom: 15px;
        }
        .form-group label {
            display: block;
            margin-bottom: 5px;
            color: #555;
        }
        .form-group input, .form-group select {
            width: 100%;
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
            box-sizing: border-box;
        }
        .btn {
            background: #2d3eaf;
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
        }
        .btn:hover {
            background: #1f2e90;
        }
        .btn-remove {
            background: #dc3545;
        }
        .btn-remove:hover {
            background: #c82333;
        }
        table { 
            width: 100%; 
            border-collapse: collapse; 
            margin-top: 20px; 
        }
        th, td { 
            padding: 12px; 
            text-align: left; 
            border-bottom: 1px solid #ddd; 
        }
        th { 
            background-color: #f8f9fa; 
            color: #2d3eaf; 
            font-weight: 600; 
        }
        tr:hover { 
            background-color: #f5f6ff; 
        }
        .search-box { 
            margin-bottom: 20px; 
            padding: 10px; 
            background: #f8f9fa; 
            border-radius: 5px; 
        }
        .search-box input { 
            padding: 8px; 
            width: 200px; 
            border: 1px solid #ddd; 
            border-radius: 4px; 
        }
        .search-box button { 
            padding: 8px 16px; 
            background: #2d3eaf; 
            color: white; 
            border: none; 
            border-radius: 4px; 
            cursor: pointer; 
        }
        .search-box button:hover { 
            background: #1f2e90; 
        }
        .apartment-number {
            color: #666;
            font-size: 0.9em;
        }
    </style>
</head>
<body>
<div class="container">
    <a class="return-btn" href="adminDashboard.jsp">&larr; Return to Dashboard</a>
    <h1>Manage Vehicles</h1>

    <div class="add-form">
        <h2>Add New Vehicle</h2>
        <form method="post" action="ManageVehiclesServlet">
            <input type="hidden" name="action" value="add">
            <div class="form-group">
                <label for="number">Vehicle Number:</label>
                <input type="text" id="number" name="number" required>
            </div>
            <div class="form-group">
                <label for="type">Vehicle Type:</label>
                <select id="type" name="type" required>
                    <option value="Car">Car</option>
                    <option value="Bike">Bike</option>
                    <option value="Scooter">Scooter</option>
                </select>
            </div>
            <div class="form-group">
                <label for="resident">Owner:</label>
                <select id="resident" name="resident" required onchange="updateApartmentNumber()">
                    <option value="">Select Owner</option>
                    <% for (Map<String, Object> resident : residents) { %>
                        <option value="<%= resident.get("apartmentNumber") %>" 
                                data-apartment="<%= resident.get("apartmentNumber") %>">
                            <%= resident.get("name") %> (Apt <%= resident.get("apartmentNumber") %>)
                        </option>
                    <% } %>
                </select>
            </div>
            <div class="form-group">
                <label for="apartmentNumber">Apartment Number:</label>
                <input type="number" id="apartmentNumber" name="apartmentNumber" readonly>
            </div>
            <button type="submit" class="btn">Add Vehicle</button>
        </form>
    </div>

    <div class="search-box">
        <input type="text" id="searchInput" placeholder="Search by vehicle number or owner...">
        <button onclick="searchVehicles()">Search</button>
    </div>

    <table>
        <thead>
            <tr>
                <th>Vehicle Number</th>
                <th>Type</th>
                <th>Owner</th>
                <th>Apartment Number</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
            <% for (Map<String, Object> vehicle : vehicles) { %>
                <tr>
                    <td><%= vehicle.get("number") %></td>
                    <td><%= vehicle.get("type") %></td>
                    <td><%= vehicle.get("residentName") %></td>
                    <td><%= vehicle.get("apartmentNumber") %></td>
                    <td>
                        <form method="post" action="ManageVehiclesServlet" style="display: inline;">
                            <input type="hidden" name="action" value="remove">
                            <input type="hidden" name="number" value="<%= vehicle.get("number") %>">
                            <button type="submit" class="btn btn-remove" onclick="return confirm('Are you sure you want to remove this vehicle?')">Remove</button>
                        </form>
                    </td>
                </tr>
            <% } %>
        </tbody>
    </table>
</div>

<script>
function updateApartmentNumber() {
    const residentSelect = document.getElementById('resident');
    const apartmentInput = document.getElementById('apartmentNumber');
    const selectedOption = residentSelect.options[residentSelect.selectedIndex];
    
    if (selectedOption.value) {
        apartmentInput.value = selectedOption.value;
    } else {
        apartmentInput.value = '';
    }
}

function searchVehicles() {
    const input = document.getElementById('searchInput');
    const filter = input.value.toLowerCase();
    const table = document.querySelector('table');
    const rows = table.getElementsByTagName('tr');

    for (let i = 1; i < rows.length; i++) {
        const row = rows[i];
        const cells = row.getElementsByTagName('td');
        let found = false;

        for (let j = 0; j < cells.length - 1; j++) {
            const cell = cells[j];
            if (cell.textContent.toLowerCase().indexOf(filter) > -1) {
                found = true;
                break;
            }
        }

        row.style.display = found ? '' : 'none';
    }
}
</script>
</body>
</html> 