<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>404 — Page Not Found — SocietEase</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
    <div class="landing-hero" style="min-height:100vh;">
        <div class="landing-content">
            <div style="font-size:6rem;font-weight:800;opacity:0.3;">404</div>
            <h1 style="font-size:2rem;">Page Not Found</h1>
            <p>The page you're looking for doesn't exist or has been moved.</p>
            <div class="landing-actions">
                <a href="${pageContext.request.contextPath}/" class="btn btn-white btn-lg">← Go Home</a>
                <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-outline btn-lg">Login</a>
            </div>
        </div>
    </div>
</body>
</html>
