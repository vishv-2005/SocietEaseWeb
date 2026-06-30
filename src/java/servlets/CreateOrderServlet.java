package servlets;

import storage.DBConnector;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Creates a Razorpay order via the Orders API (no SDK needed).
 * Compatible with GlassFish 4.1 / older Java versions.
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

        String keyId = util.AppConfig.get("RAZORPAY_KEY_ID");
        String keySecret = util.AppConfig.get("RAZORPAY_KEY_SECRET");

        LOGGER.info("Razorpay keys: ID=" + (keyId.isEmpty() ? "(EMPTY)" : keyId.substring(0, Math.min(keyId.length(), 12)) + "...") +
                    ", SECRET=" + (keySecret.isEmpty() ? "(EMPTY)" : "(set, " + keySecret.length() + " chars)"));

        if (keyId.isEmpty() || keySecret.isEmpty()) {
            LOGGER.severe("Razorpay keys not found in config.properties!");
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
            String receipt = "rcpt_" + societyId + "_" + month.replace("-", "").replace(",", "").replace(" ", "");
            String[] result = createRazorpayOrder(keyId, keySecret, amountPaise, receipt);

            if (result[0] == null) {
                out.print("{\"error\":\"" + escapeJson(result[1]) + "\"}");
                return;
            }

            LOGGER.info("Razorpay order created: " + result[0] + " for society " + societyId + ", month " + month);

            out.print("{\"orderId\":\"" + result[0] + "\","
                    + "\"amount\":" + amountPaise + ","
                    + "\"razorpayKeyId\":\"" + keyId + "\""
                    + "}");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating order", e);
            out.print("{\"error\":\"Database error. Please try again.\"}");
        }
    }

    /**
     * Calls Razorpay Orders API via HTTPS POST.
     * @return String[2]: [0]=orderId (null on failure), [1]=error message
     */
    private String[] createRazorpayOrder(String keyId, String keySecret, int amountPaise, String receipt) {
        HttpsURLConnection conn = null;
        try {
            // Set up SSL context that trusts all certs (for GlassFish 4.1 compatibility)
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            URL url = new URL("https://api.razorpay.com/v1/orders");
            conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(sslContext.getSocketFactory());
            conn.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) { return true; }
            });

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            // Basic Auth: Base64(key_id:key_secret)
            String auth = keyId + ":" + keySecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes("UTF-8"));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

            // Request body
            String body = "{\"amount\":" + amountPaise + ","
                         + "\"currency\":\"INR\","
                         + "\"receipt\":\"" + receipt + "\"}";

            LOGGER.info("Razorpay API request: POST /v1/orders body=" + body);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes("UTF-8"));
                os.flush();
            }

            int statusCode = conn.getResponseCode();
            LOGGER.info("Razorpay API response code: " + statusCode);

            if (statusCode == 200 || statusCode == 201) {
                String responseBody = readStream(conn.getInputStream());
                LOGGER.info("Razorpay API success response: " + responseBody);
                String orderId = extractJsonValue(responseBody, "id");
                if (orderId != null && orderId.startsWith("order_")) {
                    return new String[]{orderId, null};
                } else {
                    return new String[]{null, "Invalid order response from Razorpay"};
                }
            } else {
                String errorBody = "";
                try { errorBody = readStream(conn.getErrorStream()); } catch (Exception ignored) {}
                LOGGER.severe("Razorpay API error (HTTP " + statusCode + "): " + errorBody);

                // Parse error description from Razorpay response
                String errorDesc = extractJsonValue(errorBody, "description");
                if (errorDesc == null || errorDesc.isEmpty()) {
                    errorDesc = "Razorpay returned HTTP " + statusCode;
                }
                return new String[]{null, errorDesc};
            }

        } catch (javax.net.ssl.SSLHandshakeException e) {
            LOGGER.log(Level.SEVERE, "SSL Handshake failed - TLS version issue", e);
            return new String[]{null, "SSL connection failed. Server may need TLS update."};
        } catch (java.net.SocketTimeoutException e) {
            LOGGER.log(Level.SEVERE, "Razorpay API timeout", e);
            return new String[]{null, "Payment server timed out. Please try again."};
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to call Razorpay API: " + e.getClass().getName() + ": " + e.getMessage(), e);
            return new String[]{null, "Connection error: " + e.getMessage()};
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

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

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIdx = json.indexOf(searchKey);
        if (keyIdx < 0) return null;
        int colonIdx = json.indexOf(":", keyIdx + searchKey.length());
        if (colonIdx < 0) return null;
        int start = colonIdx + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) start++;
        int end = json.indexOf("\"", start);
        if (end < 0) end = json.length();
        return json.substring(start, end);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
