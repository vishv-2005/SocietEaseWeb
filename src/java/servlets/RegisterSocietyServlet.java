package servlets;

import service.ApartmentGenerator;
import storage.DBConnector;
import util.InputValidator;
import util.PasswordUtil;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Handles new society registration.
 * Creates the society, generates towers/apartments, and creates the RP user account.
 */
@WebServlet(name = "RegisterSocietyServlet", urlPatterns = {"/RegisterSocietyServlet"})
public class RegisterSocietyServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RegisterSocietyServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Validate all inputs
            String societyName = InputValidator.requireNonEmpty(request.getParameter("societyName"), "Society Name");
            String address = InputValidator.requireNonEmpty(request.getParameter("address"), "Address");
            String city = InputValidator.requireNonEmpty(request.getParameter("city"), "City");
            String state = InputValidator.requireNonEmpty(request.getParameter("state"), "State");
            String pincode = request.getParameter("pincode");
            if (!InputValidator.isValidPincode(pincode)) {
                throw new IllegalArgumentException("Please enter a valid pincode.");
            }

            int totalTowers = InputValidator.requirePositiveRange(request.getParameter("totalTowers"), "Number of Towers", 1, 26);
            int floorsPerTower = InputValidator.requirePositiveRange(request.getParameter("floorsPerTower"), "Floors per Tower", 1, 50);
            int unitsPerFloor = InputValidator.requirePositiveRange(request.getParameter("unitsPerFloor"), "Units per Floor", 1, 20);

            String rpName = InputValidator.requireNonEmpty(request.getParameter("rpName"), "Your Name");
            String rpEmail = request.getParameter("rpEmail");
            if (!InputValidator.isValidEmail(rpEmail)) {
                throw new IllegalArgumentException("Please enter a valid email address.");
            }
            rpEmail = rpEmail.trim().toLowerCase();

            String rpPassword = request.getParameter("rpPassword");
            if (!InputValidator.isValidPassword(rpPassword)) {
                throw new IllegalArgumentException("Password must be at least 8 characters with uppercase, lowercase, and digit.");
            }

            // Check if email already exists
            try (Connection conn = DBConnector.getConnection()) {
                String checkSql = "SELECT user_id FROM user WHERE email = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, rpEmail);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            request.setAttribute("error", "An account with this email already exists. Please login instead.");
                            request.getRequestDispatcher("register.jsp").forward(request, response);
                            return;
                        }
                    }
                }
            }

            // Create society in a transaction
            int societyId;
            try (Connection conn = DBConnector.getConnection()) {
                conn.setAutoCommit(false);

                try {
                    // 1. Insert society
                    String societySql = "INSERT INTO society (name, address, city, state, pincode, total_towers, floors_per_tower, units_per_floor) " +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(societySql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, societyName);
                        stmt.setString(2, address);
                        stmt.setString(3, city);
                        stmt.setString(4, state);
                        stmt.setString(5, pincode);
                        stmt.setInt(6, totalTowers);
                        stmt.setInt(7, floorsPerTower);
                        stmt.setInt(8, unitsPerFloor);
                        stmt.executeUpdate();

                        try (ResultSet keys = stmt.getGeneratedKeys()) {
                            keys.next();
                            societyId = keys.getInt(1);
                        }
                    }

                    // 2. Create RP user account
                    String hashedPassword = PasswordUtil.hashPassword(rpPassword);
                    String userSql = "INSERT INTO user (society_id, email, password, role, full_name) VALUES (?, ?, ?, 'RP', ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
                        stmt.setInt(1, societyId);
                        stmt.setString(2, rpEmail);
                        stmt.setString(3, hashedPassword);
                        stmt.setString(4, rpName);
                        stmt.executeUpdate();
                    }

                    conn.commit();
                    LOGGER.info("Society registered: " + societyName + " (ID: " + societyId + ") by " + rpEmail);

                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            }

            // 3. Generate towers and apartments (separate transaction inside ApartmentGenerator)
            int totalApartments = ApartmentGenerator.generateApartments(societyId, totalTowers, floorsPerTower, unitsPerFloor);

            LOGGER.info("Registration complete. " + totalApartments + " apartments created for society: " + societyName);

            // Redirect to login with success message
            response.sendRedirect(request.getContextPath() + "/login.jsp?msg=registered&apartments=" + totalApartments);

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("register.jsp").forward(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during registration", e);
            request.setAttribute("error", "Registration failed. Please try again later.");
            request.getRequestDispatcher("register.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("register.jsp");
    }
}
