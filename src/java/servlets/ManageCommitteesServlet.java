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
 * Manages committees with multi-tenant support. Bugs fixed:
 * - Removed redirect from finally block
 * - Proper error handling
 */
@WebServlet("/ManageCommitteesServlet")
public class ManageCommitteesServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ManageCommitteesServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int societyId = (int) session.getAttribute("societyId");

        List<Map<String, Object>> committees = new ArrayList<>();
        List<Map<String, Object>> apartments = new ArrayList<>();

        try (Connection conn = DBConnector.getConnection()) {
            // Fetch committees
            String sql = "SELECT c.committee_id, c.name, c.description, c.head_resident_id " +
                         "FROM committee c WHERE c.society_id=? ORDER BY c.name";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, societyId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> committee = new HashMap<>();
                        committee.put("committeeId", rs.getInt("committee_id"));
                        committee.put("name", rs.getString("name"));
                        committee.put("description", rs.getString("description"));
                        committee.put("headResidentId", rs.getInt("head_resident_id"));
                        committees.add(committee);
                    }
                }
            }

            // Fetch occupied apartments for dropdown
            String aptSql = "SELECT a.apartment_id, a.apartment_label FROM apartment a " +
                            "WHERE a.society_id=? AND a.status != 'VACANT' ORDER BY a.apartment_label";
            try (PreparedStatement stmt = conn.prepareStatement(aptSql)) {
                stmt.setInt(1, societyId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> apt = new HashMap<>();
                        apt.put("apartmentId", rs.getInt("apartment_id"));
                        apt.put("label", rs.getString("apartment_label"));
                        apartments.add(apt);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading committees", e);
            throw new ServletException("Failed to load committees.", e);
        }

        request.setAttribute("committees", committees);
        request.setAttribute("apartments", apartments);
        request.getRequestDispatcher("/admin/manageCommittees.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int societyId = (int) session.getAttribute("societyId");

        try {
            String action = InputValidator.requireNonEmpty(request.getParameter("action"), "Action");

            try (Connection conn = DBConnector.getConnection()) {
                if ("create".equals(action)) {
                    String name = InputValidator.requireNonEmpty(request.getParameter("name"), "Committee Name");
                    String description = request.getParameter("description");

                    String sql = "INSERT INTO committee (society_id, name, description) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, societyId);
                        stmt.setString(2, name);
                        stmt.setString(3, description);
                        stmt.executeUpdate();
                    }
                } else if ("edit".equals(action)) {
                    int committeeId = InputValidator.requireInt(request.getParameter("committeeId"), "Committee ID");
                    String name = InputValidator.requireNonEmpty(request.getParameter("name"), "Committee Name");
                    String description = request.getParameter("description");

                    String sql = "UPDATE committee SET name=?, description=? WHERE committee_id=? AND society_id=?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, name);
                        stmt.setString(2, description);
                        stmt.setInt(3, committeeId);
                        stmt.setInt(4, societyId);
                        stmt.executeUpdate();
                    }
                } else if ("dissolve".equals(action)) {
                    int committeeId = InputValidator.requireInt(request.getParameter("committeeId"), "Committee ID");

                    // Nullify committee reference in notices (don't delete notices)
                    String nullifyNotices = "UPDATE notice SET committee_id=NULL WHERE committee_id=? AND society_id=?";
                    try (PreparedStatement stmt = conn.prepareStatement(nullifyNotices)) {
                        stmt.setInt(1, committeeId);
                        stmt.setInt(2, societyId);
                        stmt.executeUpdate();
                    }

                    // Delete committee
                    String sql = "DELETE FROM committee WHERE committee_id=? AND society_id=?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, committeeId);
                        stmt.setInt(2, societyId);
                        stmt.executeUpdate();
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            request.getSession().setAttribute("errorMessage", e.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error managing committee", e);
            request.getSession().setAttribute("errorMessage", "An error occurred. Please try again.");
        }

        response.sendRedirect("ManageCommitteesServlet");
    }
}
