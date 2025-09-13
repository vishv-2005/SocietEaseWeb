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

@WebServlet("/ManageComplaintsServlet")
public class ManageComplaintsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Map<String, Object>> complaints = new ArrayList<>();
        try (Connection conn = DBConnector.getConnection()) {
            String sql = "SELECT c.*, r.name as residentName, r.contactInformation " +
                        "FROM complaint c " +
                        "LEFT JOIN resident r ON c.apartmentNumber = r.apartmentNumber " +
                        "ORDER BY c.dateFiled DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> complaint = new HashMap<>();
                    complaint.put("complaintID", rs.getInt("complaintID"));
                    complaint.put("apartmentNumber", rs.getInt("apartmentNumber"));
                    complaint.put("description", rs.getString("description"));
                    complaint.put("dateFiled", rs.getDate("dateFiled"));
                    complaint.put("status", rs.getString("status"));
                    complaint.put("residentName", rs.getString("residentName"));
                    complaint.put("contactInformation", rs.getString("contactInformation"));
                    complaints.add(complaint);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        request.setAttribute("complaints", complaints);
        request.getRequestDispatcher("manageComplaints.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        int complaintID = Integer.parseInt(request.getParameter("complaintID"));
        
        try (Connection conn = DBConnector.getConnection()) {
            if ("resolve".equals(action)) {
                String sql = "DELETE FROM complaint WHERE complaintID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, complaintID);
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        
        response.sendRedirect("ManageComplaintsServlet");
    }
} 