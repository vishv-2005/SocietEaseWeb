package servlets;

import storage.DBConnector;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/ManageCommitteesServlet")
public class ManageCommitteesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Map<String, Object>> committees = new ArrayList<>();
        List<Map<String, Object>> residents = new ArrayList<>();
        String errorMessage = null;

        try (Connection conn = DBConnector.getConnection()) {
            // Fetch all committees
            String sql = "SELECT c.*, r.name as headName, r.contactInformation as headContact "
                    + "FROM committee c "
                    + "LEFT JOIN resident r ON c.apartmentNumber = r.apartmentNumber "
                    + "ORDER BY c.name";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> committee = new HashMap<>();
                    committee.put("committeeID", rs.getInt("committeeID"));
                    committee.put("name", rs.getString("name"));
                    committee.put("description", rs.getString("description"));
                    committee.put("head", rs.getString("head"));
                    committee.put("apartmentNumber", rs.getInt("apartmentNumber"));
                    committee.put("headName", rs.getString("headName"));
                    committee.put("headContact", rs.getString("headContact"));
                    committees.add(committee);
                }
            }

            // Fetch all residents for the dropdown
            String residentSql = "SELECT apartmentNumber, name FROM resident WHERE name IS NOT NULL ORDER BY apartmentNumber";
            try (PreparedStatement stmt = conn.prepareStatement(residentSql);
                    ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> resident = new HashMap<>();
                    resident.put("apartmentNumber", rs.getInt("apartmentNumber"));
                    resident.put("name", rs.getString("name"));
                    residents.add(resident);
                }
            }
        } catch (SQLException e) {
            errorMessage = "Database error: " + e.getMessage();
            request.setAttribute("errorMessage", errorMessage);
            throw new ServletException(errorMessage, e);
        }

        request.setAttribute("committees", committees);
        request.setAttribute("residents", residents);
        request.getRequestDispatcher("manageCommittees.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String errorMessage = null;

        try (Connection conn = DBConnector.getConnection()) {
            if ("create".equals(action)) {
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String head = request.getParameter("head");
                int apartmentNumber = Integer.parseInt(request.getParameter("apartmentNumber"));

                String sql = "INSERT INTO committee (name, description, head, apartmentNumber) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, name);
                    stmt.setString(2, description);
                    stmt.setString(3, head);
                    stmt.setInt(4, apartmentNumber);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    errorMessage = "Error creating committee: " + e.getMessage();
                    request.setAttribute("errorMessage", errorMessage);
                    throw new ServletException(errorMessage, e);
                }
            } else if ("edit".equals(action)) {
                int committeeID = Integer.parseInt(request.getParameter("committeeID"));
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String head = request.getParameter("head");
                int apartmentNumber = Integer.parseInt(request.getParameter("apartmentNumber"));

                String sql = "UPDATE committee SET name=?, description=?, head=?, apartmentNumber=? WHERE committeeID=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, name);
                    stmt.setString(2, description);
                    stmt.setString(3, head);
                    stmt.setInt(4, apartmentNumber);
                    stmt.setInt(5, committeeID);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    errorMessage = "Error updating committee: " + e.getMessage();
                    request.setAttribute("errorMessage", errorMessage);
                    throw new ServletException(errorMessage, e);
                }
            } else if ("dissolve".equals(action)) {
                int committeeID = Integer.parseInt(request.getParameter("committeeID"));

                // First, attempt to delete any associated notices
                String deleteNoticesSql = "DELETE FROM notice WHERE issuedBy = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteNoticesSql)) {
                    deleteStmt.setInt(1, committeeID);
                    deleteStmt.executeUpdate();
                } catch (SQLException e) {
                    errorMessage = "Error deleting associated notices: " + e.getMessage();
                    request.setAttribute("errorMessage", errorMessage);
                    throw new ServletException(errorMessage, e);
                }

                // Then, delete the committee
                String deleteCommitteeSql = "DELETE FROM committee WHERE committeeID=?";
                try (PreparedStatement committeeStmt = conn.prepareStatement(deleteCommitteeSql)) {
                    committeeStmt.setInt(1, committeeID);
                    int rowsAffected = committeeStmt.executeUpdate();
                    if (rowsAffected == 0) {
                        errorMessage = "Committee with ID " + committeeID + " not found.";
                    }
                } catch (SQLException e) {
                    errorMessage = "Error dissolving committee: " + e.getMessage();
                    request.setAttribute("errorMessage", errorMessage);
                    throw new ServletException(errorMessage, e);
                }
            }
        } catch (SQLException e) {
            errorMessage = "Database connection error: " + e.getMessage();
            request.setAttribute("errorMessage", errorMessage);
            throw new ServletException(errorMessage, e);
        } finally {
            response.sendRedirect("ManageCommitteesServlet");
        }
    }
}
