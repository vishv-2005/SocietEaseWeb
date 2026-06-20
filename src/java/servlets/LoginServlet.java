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
                            session.setAttribute("userId", rs.getInt("user_id"));
                            session.setAttribute("societyId", rs.getInt("society_id"));
                            session.setAttribute("userEmail", rs.getString("email"));
                            session.setAttribute("userRole", rs.getString("role"));
                            session.setAttribute("userName", rs.getString("full_name"));
                            session.setAttribute("societyName", rs.getString("society_name"));
                            session.setMaxInactiveInterval(30 * 60); // 30 minutes

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
