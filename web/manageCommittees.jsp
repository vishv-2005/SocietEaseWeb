<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    if (request.getAttribute("committees") == null || request.getAttribute("residents") == null) {
        response.sendRedirect("ManageCommitteesServlet");
        return;
    }
    List<Map<String, Object>> committees = (List<Map<String, Object>>) request.getAttribute("committees");
    List<Map<String, Object>> residents = (List<Map<String, Object>>) request.getAttribute("residents");
    String errorMessage = (String) request.getAttribute("errorMessage");
    if (errorMessage != null) {
%>
<div style="color: red; text-align: center; margin-top: 20px;">
    <%= errorMessage%>
</div>
<%
    }
%>
<!DOCTYPE html>
<html>
    <head>
        <title>Manage Committees</title>
        <style>
            body { font-family: 'Segoe UI', sans-serif; background: #f7f7fc; margin: 0; }
            .container { margin: 40px auto; max-width: 1200px; background: #fff; border-radius: 10px; box-shadow: 0 0 12px #0002; padding: 32px; }
            .return-btn { display: inline-block; margin-bottom: 18px; background: #2d3eaf; color: #fff; padding: 8px 18px; border-radius: 5px; text-decoration: none; font-size: 15px; font-weight: 500; }
            .return-btn:hover { background: #1f2e90; }
            h1 { text-align: center; color: #2d3eaf; margin-bottom: 30px; }
            .create-btn { background: #52c41a; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; font-size: 14px; margin-bottom: 20px; }
            .create-btn:hover { background: #389e0d; }
            .committee-list { display: flex; flex-wrap: wrap; gap: 20px; }
            .committee-card { background: #f9faff; border-radius: 10px; box-shadow: 0 2px 8px #0001; padding: 20px; width: calc(50% - 20px); min-width: 300px; }
            .committee-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
            .committee-name { font-size: 18px; font-weight: 600; color: #2d3eaf; margin: 0; }
            .committee-actions { display: flex; gap: 8px; }
            .edit-btn, .dissolve-btn { padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 13px; border: none; }
            .edit-btn { background: #1890ff; color: white; }
            .edit-btn:hover { background: #096dd9; }
            .dissolve-btn { background: #ff4d4f; color: white; }
            .dissolve-btn:hover { background: #cf1322; }
            .committee-info { margin-bottom: 10px; }
            .committee-info p { margin: 5px 0; color: #333; }
            .committee-info strong { color: #2d3eaf; }
            .modal { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); }
            .modal-content { background: white; width: 90%; max-width: 500px; margin: 50px auto; padding: 20px; border-radius: 8px; }
            .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
            .modal-header h2 { margin: 0; color: #2d3eaf; }
            .close-btn { background: none; border: none; font-size: 20px; cursor: pointer; color: #666; }
            .form-group { margin-bottom: 15px; }
            .form-group label { display: block; margin-bottom: 5px; color: #333; }
            .form-group input, .form-group select, .form-group textarea { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
            .form-group textarea { height: 100px; resize: vertical; }
            .submit-btn { background: #2d3eaf; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; }
            .submit-btn:hover { background: #1f2e90; }
        </style>
    </head>
    <body>
        <div class="container">
            <a class="return-btn" href="adminDashboard.jsp">&larr; Return to Dashboard</a>
            <h1>Manage Committees</h1>

            <button class="create-btn" onclick="showCreateModal()">Create New Committee</button>

            <div class="committee-list">
                <% for (Map<String, Object> committee : committees) {%>
                <div class="committee-card">
                    <div class="committee-header">
                        <h3 class="committee-name"><%= committee.get("name")%></h3>
                        <div class="committee-actions">
                            <button class="edit-btn" onclick="showEditModal(<%= committee.get("committeeID")%>, '<%= committee.get("name")%>', '<%= committee.get("description")%>', '<%= committee.get("head")%>', <%= committee.get("apartmentNumber")%>)">Edit</button>
                            <form method="post" action="ManageCommitteesServlet" style="display: inline;" onsubmit="return confirm('Are you sure you want to dissolve this committee?');">
                                <input type="hidden" name="action" value="dissolve">
                                <input type="hidden" name="committeeID" value="<%= committee.get("committeeID")%>">
                                <button type="submit" class="dissolve-btn">Dissolve</button>
                            </form>
                        </div>
                    </div>
                    <div class="committee-info">
                        <p><strong>Description:</strong> <%= committee.get("description")%></p>
                        <p><strong>Head:</strong> <%= committee.get("head")%></p>
                        <p><strong>Apartment:</strong> <%= committee.get("apartmentNumber")%></p>
                        <p><strong>Contact:</strong> <%= committee.get("headContact")%></p>
                    </div>
                </div>
                <% } %>
            </div>
        </div>

        <div id="createModal" class="modal">
            <div class="modal-content">
                <div class="modal-header">
                    <h2>Create New Committee</h2>
                    <button class="close-btn" onclick="hideModal('createModal')">&times;</button>
                </div>
                <form method="post" action="ManageCommitteesServlet">
                    <input type="hidden" name="action" value="create">
                    <div class="form-group">
                        <label for="name">Committee Name</label>
                        <input type="text" id="name" name="name" required>
                    </div>
                    <div class="form-group">
                        <label for="description">Description</label>
                        <textarea id="description" name="description" required></textarea>
                    </div>
                    <div class="form-group">
                        <label for="head">Head Name</label>
                        <input type="text" id="head" name="head" required>
                    </div>
                    <div class="form-group">
                        <label for="apartmentNumber">Head's Apartment</label>
                        <select id="apartmentNumber" name="apartmentNumber" required>
                            <option value="">Select Apartment</option>
                            <% for (Map<String, Object> resident : residents) {%>
                            <option value="<%= resident.get("apartmentNumber")%>">
                                <%= resident.get("name")%> (Apt <%= resident.get("apartmentNumber")%>)
                            </option>
                            <% } %>
                        </select>
                    </div>
                    <button type="submit" class="submit-btn">Create Committee</button>
                </form>
            </div>
        </div>

        <div id="editModal" class="modal">
            <div class="modal-content">
                <div class="modal-header">
                    <h2>Edit Committee</h2>
                    <button class="close-btn" onclick="hideModal('editModal')">&times;</button>
                </div>
                <form method="post" action="ManageCommitteesServlet">
                    <input type="hidden" name="action" value="edit">
                    <input type="hidden" id="editCommitteeID" name="committeeID">
                    <div class="form-group">
                        <label for="editName">Committee Name</label>
                        <input type="text" id="editName" name="name" required>
                    </div>
                    <div class="form-group">
                        <label for="editDescription">Description</label>
                        <textarea id="editDescription" name="description" required></textarea>
                    </div>
                    <div class="form-group">
                        <label for="editHead">Head Name</label>
                        <input type="text" id="editHead" name="head" required>
                    </div>
                    <div class="form-group">
                        <label for="editApartmentNumber">Head's Apartment</label>
                        <select id="editApartmentNumber" name="apartmentNumber" required>
                            <option value="">Select Apartment</option>
                            <% for (Map<String, Object> resident : residents) {%>
                            <option value="<%= resident.get("apartmentNumber")%>">
                                <%= resident.get("name")%> (Apt <%= resident.get("apartmentNumber")%>)
                            </option>
                            <% }%>
                        </select>
                    </div>
                    <button type="submit" class="submit-btn">Update Committee</button>
                </form>
            </div>
        </div>

        <script>
            function showCreateModal() {
                document.getElementById('createModal').style.display = 'block';
            }

            function showEditModal(id, name, description, head, apartmentNumber) {
                document.getElementById('editCommitteeID').value = id;
                document.getElementById('editName').value = name;
                document.getElementById('editDescription').value = description;
                document.getElementById('editHead').value = head;
                document.getElementById('editApartmentNumber').value = apartmentNumber;
                document.getElementById('editModal').style.display = 'block';
            }

            function hideModal(modalId) {
                document.getElementById(modalId).style.display = 'none';
            }

            // Close modal when clicking outside
            window.onclick = function (event) {
                if (event.target.className === 'modal') {
                    event.target.style.display = 'none';
                }
            }
        </script>
    </body>
</html>