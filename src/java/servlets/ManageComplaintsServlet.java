package servlets;

import storage.DBConnector;
import util.InputValidator;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Manages complaints with multi-tenant support.
 * Fix: "resolve" now updates status instead of deleting the record.
 */
@WebServlet("/ManageComplaintsServlet")
public class ManageComplaintsServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ManageComplaintsServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int societyId = (int) session.getAttribute("societyId");

        List<Map<String, Object>> complaints = new ArrayList<>();

        try (Connection conn = DBConnector.getConnection()) {
            String sql = "SELECT c.complaint_id, c.description, c.date_filed, c.status, c.resolved_date, " +
                         "a.apartment_label " +
                         "FROM complaint c " +
                         "JOIN apartment a ON c.apartment_id = a.apartment_id " +
                         "WHERE c.society_id=? ORDER BY c.date_filed DESC";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, societyId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> complaint = new HashMap<>();
                        complaint.put("complaintId", rs.getInt("complaint_id"));
                        complaint.put("apartmentLabel", rs.getString("apartment_label"));
                        complaint.put("description", rs.getString("description"));
                        complaint.put("dateFiled", rs.getDate("date_filed"));
                        complaint.put("status", rs.getString("status"));
                        complaint.put("resolvedDate", rs.getDate("resolved_date"));
                        complaints.add(complaint);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading complaints", e);
            throw new ServletException("Failed to load complaints.", e);
        }

        request.setAttribute("complaints", complaints);
        request.getRequestDispatcher("/admin/manageComplaints.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int societyId = (int) session.getAttribute("societyId");

        try {
            String action = InputValidator.requireNonEmpty(request.getParameter("action"), "Action");
            int complaintId = InputValidator.requireInt(request.getParameter("complaintId"), "Complaint ID");

            try (Connection conn = DBConnector.getConnection()) {
                if ("resolve".equals(action)) {
                    // Update status to RESOLVED instead of deleting
                    String sql = "UPDATE complaint SET status='RESOLVED', resolved_date=CURDATE() " +
                                 "WHERE complaint_id=? AND society_id=?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, complaintId);
                        stmt.setInt(2, societyId);
                        stmt.executeUpdate();
                    }
                } else if ("in_progress".equals(action)) {
                    String sql = "UPDATE complaint SET status='IN_PROGRESS' WHERE complaint_id=? AND society_id=?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, complaintId);
                        stmt.setInt(2, societyId);
                        stmt.executeUpdate();
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            request.getSession().setAttribute("errorMessage", e.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error managing complaint", e);
            request.getSession().setAttribute("errorMessage", "An error occurred. Please try again.");
        }

        response.sendRedirect("ManageComplaintsServlet");
    }
}