package servlets;

import storage.DBConnector;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Creates a Razorpay order via the Orders API (no SDK needed).
 * 
 * Uses HTTP POST to https://api.razorpay.com/v1/orders with Basic Auth.
 * 
 * Environment / System Properties:
 *   RAZORPAY_KEY_ID     - Your Razorpay Key ID  (e.g. rzp_test_xxx)
 *   RAZORPAY_KEY_SECRET - Your Razorpay Key Secret
 */
@WebServlet("/CreateOrderServlet")
public class CreateOrderServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CreateOrderServlet.class.getName());

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

        String keyId = getEnv("RAZORPAY_KEY_ID", "");
        String keySecret = getEnv("RAZORPAY_KEY_SECRET", "");

        if (keyId.isEmpty() || keySecret.isEmpty() || keyId.equals("rzp_test_PLACEHOLDER")) {
            LOGGER.severe("Razorpay keys not configured! RAZORPAY_KEY_ID=" + keyId);
            out.print("{\"error\":\"Payment gateway not configured. Please contact admin.\"}");
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

            // Call Razorpay Orders API
            String receipt = "rcpt_" + societyId + "_" + month.replace("-", "");
            String orderId = createRazorpayOrder(keyId, keySecret, amountPaise, receipt);

            if (orderId == null) {
                out.print("{\"error\":\"Failed to create payment order. Please try again.\"}");
                return;
            }

            LOGGER.info("Razorpay order created: " + orderId + " for society " + societyId + ", month " + month);

            out.print("{\"orderId\":\"" + orderId + "\","
                    + "\"amount\":" + amountPaise + ","
                    + "\"razorpayKeyId\":\"" + keyId + "\""
                    + "}");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating order", e);
            out.print("{\"error\":\"Database error. Please try again.\"}");
        }
    }

    /**
     * Calls Razorpay Orders API via HTTP POST.
     * POST https://api.razorpay.com/v1/orders
     * Auth: Basic (key_id:key_secret)
     * Body: {"amount":..., "currency":"INR", "receipt":"..."}
     *
     * @return The order ID string, or null on failure
     */
    private String createRazorpayOrder(String keyId, String keySecret, int amountPaise, String receipt) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://api.razorpay.com/v1/orders");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // Basic Auth: Base64(key_id:key_secret)
            String auth = keyId + ":" + keySecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes("UTF-8"));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

            // Request body
            String body = "{\"amount\":" + amountPaise + ","
                         + "\"currency\":\"INR\","
                         + "\"receipt\":\"" + receipt + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes("UTF-8"));
                os.flush();
            }

            int statusCode = conn.getResponseCode();

            if (statusCode == 200 || statusCode == 201) {
                String responseBody = readStream(conn.getInputStream());
                // Parse order ID from JSON response (simple extraction, no library needed)
                // Response format: {"id":"order_xxx", ...}
                String orderId = extractJsonValue(responseBody, "id");
                LOGGER.info("Razorpay API response OK. Order: " + orderId);
                return orderId;
            } else {
                String errorBody = readStream(conn.getErrorStream());
                LOGGER.severe("Razorpay API error (HTTP " + statusCode + "): " + errorBody);
                return null;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to call Razorpay API", e);
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /** Read an InputStream to a String. */
    private String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /** Simple JSON value extractor for a given key (no library needed). */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIdx = json.indexOf(searchKey);
        if (keyIdx < 0) return null;
        int colonIdx = json.indexOf(":", keyIdx + searchKey.length());
        if (colonIdx < 0) return null;
        // Skip whitespace and opening quote
        int start = colonIdx + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) start++;
        // Find closing quote
        int end = json.indexOf("\"", start);
        if (end < 0) end = json.length();
        return json.substring(start, end);
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) return value;
        value = System.getProperty(key);
        if (value != null && !value.isEmpty()) return value;
        return defaultValue;
    }
}
