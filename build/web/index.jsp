<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Welcome - SocietEase</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    
    <style>
        body {
            margin: 0;
            padding: 0;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #74ebd5, #ACB6E5);
            height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        
        .container {
            background-color: white;
            padding: 40px;
            padding-right:70px;
            border-radius: 15px;
            box-shadow: 0 8px 16px rgba(0,0,0,0.2);
            text-align: center;
            width: 90%;
            max-width: 400px;
        }

        h1 {
            color: #333;
            margin-bottom: 30px;
            margin-left: 50px;
        }

        .btn {
            display: inline-block;
            width: 100%;
            padding: 12px 20px;
            margin: 10px 0;
            font-size: 16px;
            color: white;
            background-color: #007BFF;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            transition: background 0.3s ease;
            text-decoration: none;
        }

        .btn:hover {
            background-color: #0056b3;
        }

        .note {
            margin-top: 20px;
            margin-left: 50px;
            font-size: 12px;
            color: #666;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Welcome to SocietEase</h1>
        <a href="adminDashboard.jsp" class="btn">Login as Admin</a>
        <a href="residentDashboard.jsp" class="btn">Login as Resident</a>
        <div class="note">Please choose your role to continue</div>
    </div>
</body>
</html>
