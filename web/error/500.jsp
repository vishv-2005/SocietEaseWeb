<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 — Server Error — SocietEase</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
    <div class="landing-hero" style="min-height:100vh;background:linear-gradient(135deg,#ef4444 0%,#b91c1c 100%);">
        <div class="landing-content">
            <div style="font-size:6rem;font-weight:800;opacity:0.3;">500</div>
            <h1 style="font-size:2rem;">Something Went Wrong</h1>
            <p>We encountered an unexpected error. Please try again later or contact support.</p>
            <div class="landing-actions">
                <a href="${pageContext.request.contextPath}/" class="btn btn-white btn-lg">← Go Home</a>
            </div>
        </div>
    </div>
</body>
</html>
