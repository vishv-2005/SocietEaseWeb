<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Database Connection</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <style>
            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                background-color: #f0f2f5;
                margin: 0;
                padding: 0;
                display: flex;
                justify-content: center;
                align-items: center;
                height: 100vh;
            }

            .container {
                background-color: #ffffff;
                padding: 30px 40px;
                border-radius: 10px;
                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
                width: 100%;
                max-width: 400px;
            }

            h2 {
                text-align: center;
                color: #333;
                margin-bottom: 25px;
            }

            label {
                display: block;
                margin-bottom: 8px;
                color: #444;
                font-weight: bold;
            }

            input[type="text"],
            input[type="password"] {
                width: 100%;
                padding: 10px;
                margin-bottom: 20px;
                border: 1px solid #ccc;
                border-radius: 5px;
                box-sizing: border-box;
                font-size: 14px;
            }

            .password-label {
                display: flex;
                justify-content: space-between;
                align-items: center;
            }

            input[type="checkbox"] {
                margin-left: 10px;
            }

            input[type="submit"] {
                background-color: #007bff;
                color: white;
                border: none;
                padding: 10px 20px;
                width: 100%;
                border-radius: 5px;
                font-size: 16px;
                cursor: pointer;
                transition: background-color 0.3s;
            }

            input[type="submit"]:hover {
                background-color: #0056b3;
            }

            .message {
                margin-top: 20px;
                text-align: center;
                font-weight: bold;
                color: #2d6a4f;
            }

            .message.error {
                color: #c1121f;
            }
        </style>

        <script>
            function togglePassword() {
                var passwordField = document.getElementById("dbpass");
                var showCheckbox = document.getElementById("show");
                if (showCheckbox.checked) {
                    passwordField.type = "text";
                } else {
                    passwordField.type = "password";
                }
            }
        </script>
    </head>
    <body>
        <section>
            <div class="container">
                <h2>Initialize Database Connection</h2>
                <form action="mysqlServlet" method="post">
                    <label for="dbuser">Database Username:</label>
                    <input type="text" id="dbuser" name="dbuser" required>

                    <div class="password-label">
                        <label for="dbpass">Database Password:</label>
                        <label>
                            <input type="checkbox" id="show" name="show" onclick="togglePassword()"> Show
                        </label>
                    </div>
                    <input type="password" id="dbpass" name="dbpass" required>
                    <input type="submit" value="Connect and Initialize">
                </form>

                <!-- Message from servlet -->
                <div class="message <%= (request.getAttribute("message") != null && request.getAttribute("message").toString().toLowerCase().contains("fail")) ? "error" : ""%>">
                    <%= request.getAttribute("message") != null ? request.getAttribute("message") : ""%>
                </div>
            </div>
        </section>
    </body>
</html>
