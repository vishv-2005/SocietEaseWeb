package servlets;

import storage.DBConnector;
import util.EncryptionUtil;
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
 * Manages helpers with encrypted PII and multi-tenant support.
 */
@WebServlet("/ManageHelperServlet")
public class ManageHelperServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ManageHelperServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int societyId = (int) session.getAttribute("societyId");

        List<Map<String, Object>> helpers = new ArrayList<>();
        List<Map<String, Object>> apartments = new ArrayList<>();

        try (Connection conn = DBConnector.getConnection()) {
            // Fetch apartments
            String aptSql = "SELECT apartment_id, apartment_label FROM apartment WHERE society_id=? ORDER BY apartment_label";
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

            // Fetch helpers
            String helperSql = "SELECT * FROM helper WHERE society_id=? AND is_active=TRUE ORDER BY name";
            try (PreparedStatement stmt = conn.prepareStatement(helperSql)) {
                stmt.setInt(1, societyId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> helper = new HashMap<>();
                        int helperId = rs.getInt("helper_id");
                        helper.put("helperId", helperId);
                        helper.put("name", rs.getString("name"));
                        helper.put("role", rs.getString("role"));
                        helper.put("aadhar", rs.getString("aadhar_encrypted") != null ?
                            EncryptionUtil.decrypt(rs.getString("aadhar_encrypted")) : "");
                        helper.put("phone", rs.getString("phone_encrypted") != null ?
                            EncryptionUtil.decrypt(rs.getString("phone_encrypted")) : "");
                        helper.put("salary", rs.getBigDecimal("salary"));

                        // Fetch assigned apartments
                        List<Map<String, Object>> assignedApts = new ArrayList<>();
                        String assignSql = "SELECT ah.apartment_id, a.apartment_label FROM apartment_helper ah " +
                                           "JOIN apartment a ON ah.apartment_id = a.apartment_id " +
                                           "WHERE ah.helper_id=? ORDER BY a.apartment_label";
                        try (PreparedStatement aStmt = conn.prepareStatement(assignSql)) {
                            aStmt.setInt(1, helperId);
                            try (ResultSet aRs = aStmt.executeQuery()) {
                                while (aRs.next()) {
                                    Map<String, Object> apt = new HashMap<>();
                                    apt.put("apartmentId", aRs.getInt("apartment_id"));
                                    apt.put("label", aRs.getString("apartment_label"));
                                    assignedApts.add(apt);
                                }
                            }
                        }
                        helper.put("apartments", assignedApts);
                        helpers.add(helper);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading helpers", e);
            throw new ServletException("Failed to load helpers.", e);
        }

        request.setAttribute("helpers", helpers);
        request.setAttribute("apartments", apartments);
        request.getRequestDispatcher("/admin/manageHelpers.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int societyId = (int) session.getAttribute("societyId");

        try {
            String action = InputValidator.requireNonEmpty(request.getParameter("action"), "Action");

            try (Connection conn = DBConnector.getConnection()) {
                if ("add".equals(action)) {
                    String name = InputValidator.requireNonEmpty(request.getParameter("name"), "Name");
                    String role = InputValidator.requireNonEmpty(request.getParameter("role"), "Role");
                    String aadhar = request.getParameter("aadhar");
                    String phone = request.getParameter("phone");
                    String salary = InputValidator.requireNonEmpty(request.getParameter("salary"), "Salary");

                    String aadharEnc = (aadhar != null && !aadhar.trim().isEmpty()) ? EncryptionUtil.encrypt(aadhar.trim()) : null;
                    String phoneEnc = (phone != null && !phone.trim().isEmpty()) ? EncryptionUtil.encrypt(phone.trim()) : null;

                    String sql = "INSERT INTO helper (society_id, name, role, aadhar_encrypted, phone_encrypted, salary) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, societyId);
                        stmt.setString(2, name);
                        stmt.setString(3, role);
                        stmt.setString(4, aadharEnc);
                        stmt.setString(5, phoneEnc);
                        stmt.setBigDecimal(6, new java.math.BigDecimal(salary));
                        stmt.executeUpdate();
                    }
                } else if ("assign".equals(action)) {
                    int helperId = InputValidator.requireInt(request.getParameter("helperId"), "Helper");
                    int apartmentId = InputValidator.requireInt(request.getParameter("apartmentId"), "Apartment");
                    String sql = "INSERT IGNORE INTO apartment_helper (apartment_id, helper_id) VALUES (?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, apartmentId);
                        stmt.setInt(2, helperId);
                        stmt.executeUpdate();
                    }
                } else if ("unassign".equals(action)) {
                    int helperId = InputValidator.requireInt(request.getParameter("helperId"), "Helper");
                    int apartmentId = InputValidator.requireInt(request.getParameter("apartmentId"), "Apartment");
                    String sql = "DELETE FROM apartment_helper WHERE apartment_id=? AND helper_id=?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, apartmentId);
                        stmt.setInt(2, helperId);
                        stmt.executeUpdate();
                    }
                } else if ("remove".equals(action)) {
                    int helperId = InputValidator.requireInt(request.getParameter("helperId"), "Helper");
                    // Soft delete
                    String sql = "UPDATE helper SET is_active=FALSE WHERE helper_id=? AND society_id=?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, helperId);
                        stmt.setInt(2, societyId);
                        stmt.executeUpdate();
                    }
                    // Remove assignments
                    String delAssign = "DELETE FROM apartment_helper WHERE helper_id=?";
                    try (PreparedStatement stmt = conn.prepareStatement(delAssign)) {
                        stmt.setInt(1, helperId);
                        stmt.executeUpdate();
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            request.getSession().setAttribute("errorMessage", e.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error managing helper", e);
            request.getSession().setAttribute("errorMessage", "An error occurred. Please try again.");
        }

        response.sendRedirect("ManageHelperServlet");
    }
}