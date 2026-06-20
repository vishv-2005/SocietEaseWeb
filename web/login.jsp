<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login — SocietEase</title>
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
    <div class="auth-page">
        <div class="auth-left">
            <div>
                <h2>Welcome back to<br>Societ<span style="color:#a5b4fc">Ease</span></h2>
                <p>Manage your society with ease. Track residents, collect maintenance, issue notices, and more.</p>
            </div>
        </div>
        <div class="auth-right">
            <div class="auth-card">
                <h2>Sign In</h2>
                <p class="subtitle">Enter your credentials to access your dashboard.</p>

                <c:if test="${param.msg == 'registered'}">
                    <div class="alert alert-success">
                        ✅ Society registered successfully! ${param.apartments} apartments created. Please log in.
                    </div>
                </c:if>
                <c:if test="${param.msg == 'loggedout'}">
                    <div class="alert alert-success">
                        You have been logged out successfully.
                    </div>
                </c:if>
                <c:if test="${not empty error}">
                    <div class="alert alert-error">
                        ⚠️ <c:out value="${error}"/>
                    </div>
                </c:if>

                <form action="LoginServlet" method="POST" id="loginForm">
                    <div class="form-group">
                        <label for="email">Email Address</label>
                        <input type="email" class="form-control" id="email" name="email"
                               placeholder="you@example.com" required autofocus>
                    </div>
                    <div class="form-group">
                        <label for="password">Password</label>
                        <input type="password" class="form-control" id="password" name="password"
                               placeholder="Enter your password" required>
                    </div>
                    <button type="submit" class="btn btn-primary btn-lg" style="width:100; margin-top:0.5rem;">
                        Sign In
                    </button>
                </form>

                <p style="text-align:center; margin-top:1.5rem; color: var(--text-secondary); font-size: 0.9rem;">
                    Don't have a society registered?
                    <a href="register.jsp" style="font-weight:600;">Register Now</a>
                </p>
                <p style="text-align:center; margin-top:0.5rem;">
                    <a href="index.jsp" style="color: var(--text-muted); font-size: 0.85rem;">← Back to Home</a>
                </p>
            </div>
        </div>
    </div>
</body>
</html>
