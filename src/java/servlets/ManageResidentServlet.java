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
 * Manages residents with encrypted PII and multi-tenant support.
 */
@WebServlet("/ManageResidentServlet")
public class ManageResidentServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ManageResidentServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int societyId = (int) session.getAttribute("societyId");

        List<Map<String, Object>> apartments = new ArrayList<>();

        try (Connection conn = DBConnector.getConnection()) {
            // Fetch all apartments for this society with their resident data
            String sql = "SELECT a.apartment_id, a.apartment_label, a.status, a.floor_number, " +
                         "t.tower_name, r.resident_id, r.name_encrypted, r.phone_encrypted, " +
                         "r.email_encrypted, r.resident_type " +
                         "FROM apartment a " +
                         "JOIN tower t ON a.tower_id = t.tower_id " +
                         "LEFT JOIN resident r ON a.apartment_id = r.apartment_id AND r.is_active = TRUE " +
                         "WHERE a.society_id = ? " +
                         "ORDER BY t.tower_name, a.floor_number, a.unit_number";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, societyId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> apt = new HashMap<>();
                        apt.put("apartmentId", rs.getInt("apartment_id"));
                        apt.put("label", rs.getString("apartment_label"));
                        apt.put("status", rs.getString("status"));
                        apt.put("towerName", rs.getString("tower_name"));
                        apt.put("floor", rs.getInt("floor_number"));

                        int residentId = rs.getInt("resident_id");
                        if (residentId > 0) {
                            apt.put("residentId", residentId);
                            apt.put("name", EncryptionUtil.decrypt(rs.getString("name_encrypted")));
                            apt.put("phone", EncryptionUtil.decrypt(rs.getString("phone_encrypted")));
                            apt.put("email", EncryptionUtil.decrypt(rs.getString("email_encrypted")));
                            apt.put("type", rs.getString("resident_type"));
                        }
                        apartments.add(apt);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading residents", e);
            throw new ServletException("Failed to load residents.", e);
        }

        request.setAttribute("apartments", apartments);
        request.getRequestDispatcher("/admin/manageResidents.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int societyId = (int) session.getAttribute("societyId");

        try {
            String action = InputValidator.requireNonEmpty(request.getParameter("action"), "Action");
            int apartmentId = InputValidator.requireInt(request.getParameter("apartmentId"), "Apartment");

            try (Connection conn = DBConnector.getConnection()) {
                if ("add".equals(action) || "update".equals(action)) {
                    String name = InputValidator.requireNonEmpty(request.getParameter("name"), "Name");
                    String phone = InputValidator.requireNonEmpty(request.getParameter("phone"), "Phone");
                    String email = InputValidator.requireNonEmpty(request.getParameter("email"), "Email");
                    String type = InputValidator.requireNonEmpty(request.getParameter("type"), "Resident Type");

                    // Encrypt PII
                    String nameEnc = EncryptionUtil.encrypt(name);
                    String phoneEnc = EncryptionUtil.encrypt(phone);
                    String emailEnc = EncryptionUtil.encrypt(email);

                    if ("add".equals(action)) {
                        // 1. Create or find User account for login
                        int userId = -1;
                        try (PreparedStatement checkStmt = conn.prepareStatement("SELECT user_id FROM user WHERE email = ?")) {
                            checkStmt.setString(1, email);
                            try (ResultSet rs = checkStmt.executeQuery()) {
                                if (rs.next()) {
                                    userId = rs.getInt("user_id");
                                }
                            }
                        }
                        
                        if (userId == -1) {
                            String defaultPassword = "Welcome@123";
                            String hashedPwd = util.PasswordUtil.hashPassword(defaultPassword);
                            String userSql = "INSERT INTO user (society_id, email, password, role, full_name) VALUES (?, ?, ?, 'RESIDENT', ?)";
                            try (PreparedStatement uStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                                uStmt.setInt(1, societyId);
                                uStmt.setString(2, email);
                                uStmt.setString(3, hashedPwd);
                                uStmt.setString(4, name);
                                uStmt.executeUpdate();
                                try (ResultSet rs = uStmt.getGeneratedKeys()) {
                                    if (rs.next()) userId = rs.getInt(1);
                                }
                            }
                        }

                        // 2. Insert Resident
                        String sql = "INSERT INTO resident (society_id, apartment_id, user_id, name_encrypted, phone_encrypted, email_encrypted, resident_type, move_in_date) " +
                                     "VALUES (?, ?, ?, ?, ?, ?, ?, CURDATE())";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setInt(1, societyId);
                            stmt.setInt(2, apartmentId);
                            if (userId > 0) {
                                stmt.setInt(3, userId);
                            } else {
                                stmt.setNull(3, java.sql.Types.INTEGER);
                            }
                            stmt.setString(4, nameEnc);
                            stmt.setString(5, phoneEnc);
                            stmt.setString(6, emailEnc);
                            stmt.setString(7, type);
                            stmt.executeUpdate();
                        }
                        
                        // 3. Send Email
                        service.EmailService.sendWelcomeEmail(societyId, email, name, "your society (Default Password: Welcome@123)");
                    } else {
                        int residentId = InputValidator.requireInt(request.getParameter("residentId"), "Resident ID");
                        String sql = "UPDATE resident SET name_encrypted=?, phone_encrypted=?, email_encrypted=?, resident_type=? " +
                                     "WHERE resident_id=? AND society_id=?";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, nameEnc);
                            stmt.setString(2, phoneEnc);
                            stmt.setString(3, emailEnc);
                            stmt.setString(4, type);
                            stmt.setInt(5, residentId);
                            stmt.setInt(6, societyId);
                            stmt.executeUpdate();
                        }
                    }

                    // Update apartment status
                    String statusVal = "OWNER".equals(type) ? "OWNER_OCCUPIED" : "TENANT_OCCUPIED";
                    String updateApt = "UPDATE apartment SET status=? WHERE apartment_id=? AND society_id=?";
                    try (PreparedStatement stmt = conn.prepareStatement(updateApt)) {
                        stmt.setString(1, statusVal);
                        stmt.setInt(2, apartmentId);
                        stmt.setInt(3, societyId);
                        stmt.executeUpdate();
                    }

                } else if ("remove".equals(action)) {
                    int residentId = InputValidator.requireInt(request.getParameter("residentId"), "Resident ID");

                    // Soft-delete resident
                    String sql = "UPDATE resident SET is_active=FALSE WHERE resident_id=? AND society_id=?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, residentId);
                        stmt.setInt(2, societyId);
                        stmt.executeUpdate();
                    }

                    // Set apartment to VACANT
                    String updateApt = "UPDATE apartment SET status='VACANT' WHERE apartment_id=? AND society_id=?";
                    try (PreparedStatement stmt = conn.prepareStatement(updateApt)) {
                        stmt.setInt(1, apartmentId);
                        stmt.setInt(2, societyId);
                        stmt.executeUpdate();
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            request.getSession().setAttribute("errorMessage", e.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error managing resident", e);
            request.getSession().setAttribute("errorMessage", "An error occurred. Please try again.");
        }

        response.sendRedirect("ManageResidentServlet");
    }
}