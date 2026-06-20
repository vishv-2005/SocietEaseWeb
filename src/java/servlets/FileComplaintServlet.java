package servlets;

import storage.DBConnector;
import util.InputValidator;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Handles complaint filing by residents.
 */
@WebServlet("/FileComplaintServlet")
public class FileComplaintServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FileComplaintServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("societyId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int societyId = (int) session.getAttribute("societyId");

        try {
            String description = InputValidator.requireNonEmpty(request.getParameter("description"), "Description");

            try (Connection conn = DBConnector.getConnection()) {
                // Get the resident's apartment (for now, use first occupied apartment)
                int apartmentId = 0;
                String aptSql = "SELECT apartment_id FROM apartment WHERE society_id=? AND status != 'VACANT' LIMIT 1";
                try (PreparedStatement stmt = conn.prepareStatement(aptSql)) {
                    stmt.setInt(1, societyId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) apartmentId = rs.getInt("apartment_id");
                    }
                }

                if (apartmentId > 0) {
                    String sql = "INSERT INTO complaint (society_id, apartment_id, description, date_filed) VALUES (?, ?, ?, CURDATE())";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, societyId);
                        stmt.setInt(2, apartmentId);
                        stmt.setString(3, description);
                        stmt.executeUpdate();
                    }
                    session.setAttribute("message", "Complaint submitted successfully! The society management will review it.");
                    session.setAttribute("messageType", "success");
                } else {
                    session.setAttribute("message", "Error: No apartment assigned to your account.");
                    session.setAttribute("messageType", "error");
                }
            }
        } catch (IllegalArgumentException e) {
            session.setAttribute("message", e.getMessage());
            session.setAttribute("messageType", "error");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error filing complaint", e);
            session.setAttribute("message", "Failed to submit complaint. Please try again.");
            session.setAttribute("messageType", "error");
        }

        response.sendRedirect(request.getContextPath() + "/resident/fileComplaint.jsp");
    }
}
