<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>SocietEase - Admin Dashboard</title>
    <style>
        body {
            margin: 0;
            font-family: 'Segoe UI', sans-serif;
            background-color: #f7f7fc;
        }

        .sidebar {
            width: 220px;
            height: 100vh;
            background-color: #2d3eaf;
            position: fixed;
            top: 0;
            left: 0;
            padding-top: 20px;
            color: white;
        }

        .sidebar h2 {
            text-align: center;
            margin-bottom: 30px;
            font-size: 22px;
        }

        .sidebar a {
            display: block;
            padding: 12px 20px;
            color: white;
            text-decoration: none;
            font-size: 14px;
        }

        .sidebar a:hover {
            background-color: #1f2e90;
        }

        .main {
            margin-left: 220px;
            padding: 20px 30px;
        }

        .header {
            text-align: center;
            margin-bottom: 30px;
        }

        .header h1 {
            font-size: 26px;
            margin: 0;
        }

        .header p {
            font-size: 14px;
            color: #444;
        }

        .stats {
            display: flex;
            justify-content: space-between;
            margin-bottom: 30px;
        }

        .stat-card {
            flex: 1;
            margin: 0 10px;
            background-color: #fff;
            border-radius: 6px;
            box-shadow: 0 0 2px rgba(0, 0, 0, 0.15);
            text-align: center;
            padding: 20px 0;
        }

        .stat-card h3 {
            margin: 0;
            font-size: 13px;
            color: #555;
        }

        .stat-card p {
            font-size: 22px;
            color: #2d3eaf;
            margin: 8px 0 0 0;
            font-weight: bold;
        }

        .section {
            background-color: #fff;
            border-radius: 6px;
            box-shadow: 0 0 2px rgba(0, 0, 0, 0.1);
            padding: 20px;
            margin-bottom: 30px;
        }

        .section h2 {
            font-size: 16px;
            color: #2d3eaf;
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin: 0 0 15px 0;
        }

        .btn {
            background-color: #2d3eaf;
            color: #fff;
            border: none;
            padding: 6px 14px;
            border-radius: 4px;
            font-size: 13px;
            cursor: pointer;
        }

        .btn:hover {
            background-color: #1f2e90;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th {
            background-color: #2d3eaf;
            color: white;
            padding: 8px;
            font-size: 13px;
        }

        td {
            padding: 8px;
            border: 1px solid #ccc;
            font-size: 13px;
            text-align: center;
        }

        .switch-btn {
            position: fixed;
            bottom: 20px;
            left: 20px;
            background-color: #f5365c;
            color: white;
            padding: 10px 16px;
            font-size: 13px;
            font-weight: bold;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }

        .switch-btn:hover {
            background-color: #d42c4b;
        }
    </style>

    <script>
        function updateDashboard() {
            fetch('adminDashboardData')
                .then(response => response.json())
                .then(data => {
                    // Update statistics
                    document.getElementById('totalResidents').textContent = data.totalResidents;
                    document.getElementById('activeComplaints').textContent = data.activeComplaints;
                    document.getElementById('totalVehicles').textContent = data.totalVehicles;

                    // Update notices table
                    const noticesTable = document.getElementById('noticesTable').getElementsByTagName('tbody')[0];
                    noticesTable.innerHTML = '';
                    data.notices.forEach(notice => {
                        const row = noticesTable.insertRow();
                        row.insertCell(0).textContent = notice.notice_id;
                        row.insertCell(1).textContent = notice.content;
                        row.insertCell(2).textContent = notice.issued_by;
                        row.insertCell(3).textContent = notice.date;
                    });

                    // Update committees table
                    const committeesTable = document.getElementById('committeesTable').getElementsByTagName('tbody')[0];
                    committeesTable.innerHTML = '';
                    data.committees.forEach(committee => {
                        const row = committeesTable.insertRow();
                        row.insertCell(0).textContent = committee.committee_id;
                        row.insertCell(1).textContent = committee.name;
                        row.insertCell(2).textContent = committee.description;
                        row.insertCell(3).textContent = committee.head_name;
                        row.insertCell(4).textContent = committee.apartment_number;
                    });
                })
                .catch(error => console.error('Error fetching dashboard data:', error));
        }

        // Update dashboard every 2 seconds
        setInterval(updateDashboard, 2000);
        
        // Initial update
        document.addEventListener('DOMContentLoaded', updateDashboard);
    </script>
</head>
<body>

<div class="sidebar">
    <h2>SocietEase</h2>
    <a href="manageResidents.jsp">Manage Residents</a>
    <a href="manageHelpers.jsp">Manage Helpers</a>
    <a href="maintenanceRecords.jsp">Maintenance Records</a>
    <a href="manageComplaints.jsp">Manage Complaints</a>
    <a href="manageCommittees.jsp">Manage Committees</a>
    <a href="issueNotice.jsp">Issue Notice</a>
    <a href="manageVehicles.jsp">Manage Vehicles</a>
</div>

<div class="main">
    <div class="header">
        <h1>Welcome to Admin Dashboard</h1>
        <p>Manage your society efficiently</p>
    </div>

    <div class="stats">
        <div class="stat-card">
            <h3>Total Residents</h3>
            <p id="totalResidents">0</p>
        </div>
        <div class="stat-card">
            <h3>Active Complaints</h3>
            <p id="activeComplaints">0</p>
        </div>
        <div class="stat-card">
            <h3>Total Vehicles</h3>
            <p id="totalVehicles">0</p>
        </div>
    </div>

    <div class="section">
        <h2>
            Recent Notices
            <a href="issueNotice.jsp" class="btn">Issue Notice</a>
        </h2>
        <table id="noticesTable">
            <thead>
                <tr>
                    <th>Notice ID</th>
                    <th>Content</th>
                    <th>Issued By</th>
                    <th>Date</th>
                </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
    </div>

    <div class="section">
        <h2>Active Committees</h2>
        <table id="committeesTable">
            <thead>
                <tr>
                    <th>Committee ID</th>
                    <th>Committee Name</th>
                    <th>Description</th>
                    <th>Head</th>
                    <th>Apartment Number</th>
                </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
    </div>
</div>

<a href="residentDashboard.jsp" class="switch-btn">Switch to Resident</a>

</body>
</html>
