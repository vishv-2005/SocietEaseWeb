<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>SocietEase - Resident Dashboard</title>
    <style>
        body {
            margin: 0;
            font-family: 'Segoe UI', sans-serif;
            background-color: #f7f7fc;
        }
        .header-bar {
            background: #2d3eaf;
            color: #fff;
            padding: 18px 32px;
            font-size: 28px;
            font-weight: bold;
            position: relative;
        }
        .switch-btn {
            position: absolute;
            top: 12px;
            right: 32px;
            background-color: #f5365c;
            color: white;
            padding: 10px 24px;
            font-size: 15px;
            font-weight: bold;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            z-index: 10;
        }
        .switch-btn:hover {
            background-color: #d42c4b;
        }
        .main {
            max-width: 1200px;
            margin: 30px auto;
            padding: 0 20px;
        }
        .welcome {
            text-align: center;
            margin-bottom: 30px;
        }
        .welcome h1 {
            font-size: 32px;
            margin-bottom: 8px;
        }
        .welcome p {
            color: #444;
            font-size: 16px;
        }
        .actions {
            display: flex;
            justify-content: center;
            gap: 32px;
            margin-bottom: 40px;
        }
        .action-card {
            background: #fff;
            border-radius: 10px;
            box-shadow: 0 0 8px #0001;
            padding: 28px 36px;
            text-align: center;
            min-width: 220px;
        }
        .action-card h2 {
            font-size: 20px;
            margin-bottom: 18px;
        }
        .action-btn {
            background: #2d3eaf;
            color: #fff;
            border: none;
            padding: 12px 32px;
            border-radius: 5px;
            font-size: 16px;
            cursor: pointer;
            font-weight: 500;
        }
        .action-btn:hover {
            background: #1f2e90;
        }
        .section {
            background: #fff;
            border-radius: 10px;
            box-shadow: 0 0 8px #0001;
            padding: 24px;
            margin-bottom: 32px;
        }
        .section h2 {
            font-size: 20px;
            color: #2d3eaf;
            margin-bottom: 18px;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }
        th {
            background: #2d3eaf;
            color: #fff;
            padding: 10px;
            font-size: 15px;
        }
        td {
            padding: 10px;
            border: 1px solid #ccc;
            font-size: 15px;
            text-align: center;
        }
    </style>
    <script>
        function updateResidentDashboard() {
            fetch('residentDashboardData')
                .then(response => response.json())
                .then(data => {
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

                    // Update helpers table
                    const helpersTable = document.getElementById('helpersTable').getElementsByTagName('tbody')[0];
                    helpersTable.innerHTML = '';
                    data.helpers.forEach(helper => {
                        const row = helpersTable.insertRow();
                        row.insertCell(0).textContent = helper.helper_id;
                        row.insertCell(1).textContent = helper.name;
                        row.insertCell(2).textContent = helper.role;
                        row.insertCell(3).textContent = helper.salary;
                        row.insertCell(4).textContent = helper.contact;
                        row.insertCell(5).textContent = helper.aadharNumber;
                    });

                    // Update committees table
                    const committeesTable = document.getElementById('committeesTable').getElementsByTagName('tbody')[0];
                    committeesTable.innerHTML = '';
                    data.committees.forEach(committee => {
                        const row = committeesTable.insertRow();
                        row.insertCell(0).textContent = committee.committee_id;
                        row.insertCell(1).textContent = committee.name;
                        row.insertCell(2).textContent = committee.description;
                        row.insertCell(3).textContent = committee.head;
                        row.insertCell(4).textContent = committee.apartment_number;
                    });
                })
                .catch(error => console.error('Error fetching resident dashboard data:', error));
        }

        setInterval(updateResidentDashboard, 2000);
        document.addEventListener('DOMContentLoaded', updateResidentDashboard);
    </script>
</head>
<body>
    <div class="header-bar">
        SocietEase
        <button class="switch-btn" onclick="location.href='adminDashboard.jsp'">Switch to Admin View</button>
    </div>
    <div class="main">
        <div class="welcome">
            <h1>Welcome to Your Dashboard</h1>
            <p>Manage your society activities</p>
        </div>
        <div class="actions">
            <div class="action-card">
                <h2>Pay Maintenance</h2>
                <button class="action-btn" onclick="location.href='payMaintenance'">Pay</button>
            </div>
            <div class="action-card">
                <h2>File Complaint</h2>
                <button class="action-btn" onclick="location.href='fileComplaint'">Open</button>
            </div>
            <div class="action-card">
                <h2>View Vehicles</h2>
                <button class="action-btn" onclick="location.href='viewVehicles.jsp'">View</button>
            </div>
        </div>
        <div class="section">
            <h2>Recent Notices</h2>
            <table id="noticesTable">
                <thead>
                    <tr>
                        <th>Notice ID</th>
                        <th>Content</th>
                        <th>Issued By</th>
                        <th>Date</th>
                    </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
        <div class="section">
            <h2>Helpers</h2>
            <table id="helpersTable">
                <thead>
                    <tr>
                        <th>Helper ID</th>
                        <th>Name</th>
                        <th>Role</th>
                        <th>Salary</th>
                        <th>Contact</th>
                        <th>Aadhar Number</th>
                    </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
        <div class="section">
            <h2>Active Committees</h2>
            <table id="committeesTable">
                <thead>
                    <tr>
                        <th>Committee ID</th>
                        <th>Name</th>
                        <th>Description</th>
                        <th>Head</th>
                        <th>Apartment Number</th>
                    </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
    </div>
</body>
</html> 