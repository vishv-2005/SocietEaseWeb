package servlets;

import storage.DBConnector;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Creates a Razorpay order for maintenance payment.
 * 
 * In TEST MODE (no Razorpay SDK), this generates a mock order.
 * When you add the Razorpay SDK, replace the mock with actual API calls.
 * 
 * Environment Variables:
 *   RAZORPAY_KEY_ID     - Your Razorpay Key ID
 *   RAZORPAY_KEY_SECRET - Your Razorpay Key Secret
 */
@WebServlet("/CreateOrderServlet")
public class CreateOrderServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CreateOrderServlet.class.getName());
    private static final String RAZORPAY_KEY_ID = getEnv("RAZORPAY_KEY_ID", "rzp_test_PLACEHOLDER");
    private static final String RAZORPAY_KEY_SECRET = getEnv("RAZORPAY_KEY_SECRET", "");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("societyId") == null) {
            out.print("{\"error\":\"Not authenticated\"}");
            return;
        }

        int societyId = (int) session.getAttribute("societyId");
        String month = request.getParameter("month");

        if (month == null || month.trim().isEmpty()) {
            out.print("{\"error\":\"Payment month is required\"}");
            return;
        }

        try (Connection conn = DBConnector.getConnection()) {
            // Get maintenance amount for this society
            int amountPaise = 100000; // Default ₹1000 in paise
            String amtSql = "SELECT maintenance_amount FROM society WHERE society_id=?";
            try (PreparedStatement stmt = conn.prepareStatement(amtSql)) {
                stmt.setInt(1, societyId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        amountPaise = rs.getBigDecimal("maintenance_amount").intValue() * 100;
                    }
                }
            }

            // Generate a mock order ID (replace with Razorpay API call when SDK is added)
            // Real implementation:
            //   RazorpayClient client = new RazorpayClient(RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET);
            //   JSONObject orderReq = new JSONObject();
            //   orderReq.put("amount", amountPaise);
            //   orderReq.put("currency", "INR");
            //   orderReq.put("receipt", "rcpt_" + societyId + "_" + month);
            //   Order order = client.orders.create(orderReq);
            //   String orderId = order.get("id");

            String orderId = "order_test_" + System.currentTimeMillis();

            LOGGER.info("Created order: " + orderId + " for society " + societyId + ", month " + month);

            out.print("{\"orderId\":\"" + orderId + "\","
                    + "\"amount\":" + amountPaise + ","
                    + "\"razorpayKeyId\":\"" + RAZORPAY_KEY_ID + "\""
                    + "}");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating order", e);
            out.print("{\"error\":\"Failed to create order. Please try again.\"}");
        }
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}
