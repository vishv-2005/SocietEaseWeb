package servlets;

import storage.DBConnector;
import util.PasswordUtil;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Handles user login authentication.
 * Validates email and password, creates session with user info.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet"})
public class LoginServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Please enter both email and password.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        email = email.trim().toLowerCase();

        try (Connection conn = DBConnector.getConnection()) {
            String sql = "SELECT u.user_id, u.society_id, u.email, u.password, u.role, u.full_name, s.name as society_name " +
                         "FROM user u JOIN society s ON u.society_id = s.society_id " +
                         "WHERE u.email = ? AND u.is_active = TRUE";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password");

                        if (PasswordUtil.verifyPassword(password, storedHash)) {
                            // Login successful — create session
                            HttpSession session = request.getSession(true);
                            int userId = rs.getInt("user_id");
                            int societyId = rs.getInt("society_id");
                            session.setAttribute("userId", userId);
                            session.setAttribute("societyId", societyId);
                            session.setAttribute("userEmail", rs.getString("email"));
                            session.setAttribute("userRole", rs.getString("role"));
                            session.setAttribute("userName", rs.getString("full_name"));
                            session.setAttribute("societyName", rs.getString("society_name"));
                            session.setMaxInactiveInterval(30 * 60); // 30 minutes

                            // Look up resident's apartment and society's maintenance amount
                            try {
                                // Get apartment_id for this user
                                String aptSql = "SELECT r.apartment_id, a.apartment_label FROM resident r " +
                                                "JOIN apartment a ON r.apartment_id = a.apartment_id " +
                                                "WHERE r.user_id=? AND r.is_active=TRUE LIMIT 1";
                                try (PreparedStatement aptStmt = conn.prepareStatement(aptSql)) {
                                    aptStmt.setInt(1, userId);
                                    try (ResultSet aptRs = aptStmt.executeQuery()) {
                                        if (aptRs.next()) {
                                            session.setAttribute("apartmentId", aptRs.getInt("apartment_id"));
                                            session.setAttribute("apartmentLabel", aptRs.getString("apartment_label"));
                                        }
                                    }
                                }
                                // Get maintenance amount for the society
                                String maintSql = "SELECT maintenance_amount FROM society WHERE society_id=?";
                                try (PreparedStatement maintStmt = conn.prepareStatement(maintSql)) {
                                    maintStmt.setInt(1, societyId);
                                    try (ResultSet maintRs = maintStmt.executeQuery()) {
                                        if (maintRs.next()) {
                                            session.setAttribute("maintenanceAmount", maintRs.getBigDecimal("maintenance_amount").intValue());
                                        }
                                    }
                                }
                            } catch (SQLException ex) {
                                LOGGER.log(Level.WARNING, "Could not load apartment/maintenance info", ex);
                            }

                            LOGGER.info("Login successful: " + email + " (role: " + rs.getString("role") + ")");

                            // Redirect based on role
                            String role = rs.getString("role");
                            if ("RP".equals(role) || "SUPER_ADMIN".equals(role)) {
                                response.sendRedirect(request.getContextPath() + "/adminDashboardData?view=dashboard");
                            } else {
                                response.sendRedirect(request.getContextPath() + "/resident/dashboard.jsp");
                            }
                            return;
                        }
                    }
                }
            }

            // Login failed
            LOGGER.warning("Failed login attempt for: " + email);
            request.setAttribute("error", "Invalid email or password.");
            request.getRequestDispatcher("login.jsp").forward(request, response);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during login", e);
            request.setAttribute("error", "An error occurred. Please try again later.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("login.jsp");
    }
}
