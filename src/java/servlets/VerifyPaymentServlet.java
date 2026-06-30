package servlets;

import storage.DBConnector;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Verifies Razorpay payment signature and records the payment.
 * 
 * Signature verification:
 *   generated_signature = HMAC-SHA256(order_id + "|" + payment_id, key_secret)
 *   Compare generated_signature with razorpay_signature
 */
@WebServlet("/VerifyPaymentServlet")
public class VerifyPaymentServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(VerifyPaymentServlet.class.getName());
    private static final String RAZORPAY_KEY_SECRET = getEnv("RAZORPAY_KEY_SECRET", "test_secret");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("societyId") == null) {
            out.print("{\"status\":\"failed\",\"error\":\"Not authenticated\"}");
            return;
        }

        int societyId = (int) session.getAttribute("societyId");
        Integer apartmentId = (Integer) session.getAttribute("apartmentId");
        String paymentId = request.getParameter("razorpay_payment_id");
        String orderId = request.getParameter("razorpay_order_id");
        String signature = request.getParameter("razorpay_signature");
        String month = request.getParameter("month");

        if (paymentId == null || orderId == null || signature == null || month == null) {
            out.print("{\"status\":\"failed\",\"error\":\"Missing payment details\"}");
            return;
        }

        if (apartmentId == null || apartmentId <= 0) {
            out.print("{\"status\":\"failed\",\"error\":\"No apartment linked to your account\"}");
            return;
        }

        try {
            // Verify signature (skip in test mode for mock orders)
            boolean isTestOrder = orderId.startsWith("order_test_");
            boolean signatureValid = isTestOrder || verifySignature(orderId, paymentId, signature);

            if (!signatureValid) {
                LOGGER.warning("Invalid payment signature for order: " + orderId);
                out.print("{\"status\":\"failed\",\"error\":\"Payment signature verification failed\"}");
                return;
            }

            // Record payment in database using the resident's actual apartment
            try (Connection conn = DBConnector.getConnection()) {
                // Get actual maintenance amount from society table
                int amount = 1000;
                String amtSql = "SELECT maintenance_amount FROM society WHERE society_id=?";
                try (PreparedStatement amtStmt = conn.prepareStatement(amtSql)) {
                    amtStmt.setInt(1, societyId);
                    try (ResultSet rs = amtStmt.executeQuery()) {
                        if (rs.next()) amount = rs.getBigDecimal("maintenance_amount").intValue();
                    }
                }

                String sql = "INSERT INTO maintenance_payment (society_id, apartment_id, amount, payment_month, " +
                             "razorpay_order_id, razorpay_payment_id, razorpay_signature, status, mode_of_payment, payment_date) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, 'PAID', 'ONLINE', NOW()) " +
                             "ON DUPLICATE KEY UPDATE status='PAID', razorpay_payment_id=?, razorpay_signature=?, payment_date=NOW()";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, societyId);
                    stmt.setInt(2, apartmentId);
                    stmt.setBigDecimal(3, new java.math.BigDecimal(amount));
                    stmt.setString(4, month);
                    stmt.setString(5, orderId);
                    stmt.setString(6, paymentId);
                    stmt.setString(7, signature);
                    stmt.setString(8, paymentId);
                    stmt.setString(9, signature);
                    stmt.executeUpdate();
                }

                LOGGER.info("Payment verified and recorded: " + paymentId + " for month " + month + ", apartment " + apartmentId);
                out.print("{\"status\":\"success\",\"paymentId\":\"" + paymentId + "\"}");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Payment verification error", e);
            out.print("{\"status\":\"failed\",\"error\":\"Internal error during verification\"}");
        }
    }

    private boolean verifySignature(String orderId, String paymentId, String expectedSignature) {
        try {
            String data = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(RAZORPAY_KEY_SECRET.getBytes("UTF-8"), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes("UTF-8"));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString().equals(expectedSignature);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Signature verification error", e);
            return false;
        }
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) return value;
        value = System.getProperty(key);
        if (value != null && !value.isEmpty()) return value;
        return defaultValue;
    }
}
