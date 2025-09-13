<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    if (request.getAttribute("committees") == null) {
        response.sendRedirect("IssueNoticeServlet");
        return;
    }
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Issue Notice - SocietEase</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <style>
            body {
                background-color: #f8f9fa;
                padding: 20px;
            }
            .notice-form {
                background-color: white;
                padding: 30px;
                border-radius: 10px;
                box-shadow: 0 0 10px rgba(0,0,0,0.1);
                max-width: 800px;
                margin: 20px auto;
            }
            .form-label {
                font-weight: 500;
            }
            .btn-primary {
                background-color: #0d6efd;
                border: none;
                padding: 10px 20px;
            }
            .btn-primary:hover {
                background-color: #0b5ed7;
            }
            .alert {
                margin-bottom: 20px;
            }
            .back-btn {
                margin-bottom: 20px;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <a href="adminDashboard.jsp" class="btn btn-secondary back-btn">← Back to Dashboard</a>
            
            <div class="notice-form">
                <h2 class="mb-4">Issue New Notice</h2>
                
                <c:if test="${not empty sessionScope.message}">
                    <div class="alert alert-${sessionScope.messageType} alert-dismissible fade show" role="alert">
                        ${sessionScope.message}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                    <c:remove var="message" scope="session"/>
                    <c:remove var="messageType" scope="session"/>
                </c:if>
                
                <form action="IssueNoticeServlet" method="POST">
                    <div class="mb-3">
                        <label for="issueDate" class="form-label">Issue Date</label>
                        <input type="date" class="form-control" id="issueDate" name="issueDate" value="${currentDate}" required>
                    </div>

                    <div class="mb-3">
                        <label for="title" class="form-label">Notice Title</label>
                        <input type="text" class="form-control" id="title" name="title" required>
                    </div>
                    
                    <div class="mb-3">
                        <label for="content" class="form-label">Notice Content</label>
                        <textarea class="form-control" id="content" name="content" rows="5" required></textarea>
                    </div>
                    
                    <div class="mb-3">
                        <label for="issuedBy" class="form-label">Issued By Committee</label>
                        <select class="form-select" id="issuedBy" name="issuedBy" required>
                            <option value="">Select a committee</option>
                            <c:if test="${empty committees}">
                                <option value="" disabled>No committees available. Please create a committee first.</option>
                            </c:if>
                            <c:forEach items="${committees}" var="committee">
                                <option value="${committee.committeeID}">${committee.name} (Head: ${committee.headName})</option>
                            </c:forEach>
                        </select>
                        <c:if test="${empty committees}">
                            <div class="text-danger mt-2">
                                No committees found. Please create a committee before issuing a notice.
                            </div>
                        </c:if>
                    </div>
                    
                    <button type="submit" class="btn btn-primary">Issue Notice</button>
                </form>
            </div>
        </div>
        
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html> 