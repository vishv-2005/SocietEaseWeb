package servlets;

import storage.DBConnector;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Displays maintenance payment records with multi-tenant support.
 */
@WebServlet("/MaintenanceRecordsServlet")
public class MaintenanceRecordsServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(MaintenanceRecordsServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int societyId = (int) session.getAttribute("societyId");

        List<Map<String, Object>> records = new ArrayList<>();

        try (Connection conn = DBConnector.getConnection()) {
            String sql = "SELECT mp.payment_id, mp.amount, mp.payment_month, mp.status, " +
                         "mp.mode_of_payment, mp.payment_date, mp.razorpay_payment_id, " +
                         "a.apartment_label " +
                         "FROM maintenance_payment mp " +
                         "JOIN apartment a ON mp.apartment_id = a.apartment_id " +
                         "WHERE mp.society_id=? " +
                         "ORDER BY mp.payment_month DESC, a.apartment_label";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, societyId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> record = new HashMap<>();
                        record.put("paymentId", rs.getInt("payment_id"));
                        record.put("apartmentLabel", rs.getString("apartment_label"));
                        record.put("amount", rs.getBigDecimal("amount"));
                        record.put("month", rs.getString("payment_month"));
                        record.put("status", rs.getString("status"));
                        record.put("mode", rs.getString("mode_of_payment"));
                        record.put("paymentDate", rs.getTimestamp("payment_date"));
                        record.put("razorpayId", rs.getString("razorpay_payment_id"));
                        records.add(record);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading maintenance records", e);
            throw new ServletException("Failed to load maintenance records.", e);
        }

        request.setAttribute("records", records);
        request.getRequestDispatcher("/admin/maintenanceRecords.jsp").forward(request, response);
    }
}