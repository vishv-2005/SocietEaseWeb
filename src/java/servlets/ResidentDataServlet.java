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
 * Serves resident-facing data as JSON.
 * Accessible by RESIDENT, RP, and SUPER_ADMIN roles.
 * 
 * Supports ?type= parameter:
 *   - notices   → returns society notices
 *   - payments  → returns resident's payment history
 *   - dashboard → returns basic stats for resident dashboard
 */
@WebServlet("/ResidentDataServlet")
public class ResidentDataServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ResidentDataServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
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
        Integer apartmentId = (Integer) session.getAttribute("apartmentId");
        String type = request.getParameter("type");

        if (type == null) type = "notices";

        try (Connection conn = DBConnector.getConnection()) {
            switch (type) {
                case "notices":
                    out.print(getNotices(conn, societyId));
                    break;
                case "payments":
                    out.print(getPaymentHistory(conn, societyId, apartmentId));
                    break;
                case "complaints":
                    out.print(getComplaints(conn, societyId, apartmentId));
                    break;
                case "dashboard":
                    out.print(getDashboardData(conn, societyId, apartmentId));
                    break;
                default:
                    out.print("{\"error\":\"Unknown type\"}");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "ResidentDataServlet error", e);
            out.print("{\"error\":\"Failed to load data\"}");
        }
        out.flush();
    }

    private String getNotices(Connection conn, int societyId) throws SQLException {
        String sql = "SELECT n.notice_id, n.title, n.content, n.notice_date, c.name as committee_name " +
                     "FROM notice n LEFT JOIN committee c ON n.committee_id = c.committee_id " +
                     "WHERE n.society_id=? ORDER BY n.notice_date DESC LIMIT 20";
        StringBuilder sb = new StringBuilder("{\"notices\":[");
        boolean first = true;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, societyId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (!first) sb.append(",");
                    sb.append("{")
                      .append("\"notice_id\":").append(rs.getInt("notice_id")).append(",")
                      .append("\"title\":\"").append(escapeJson(rs.getString("title"))).append("\",")
                      .append("\"content\":\"").append(escapeJson(rs.getString("content"))).append("\",")
                      .append("\"committee\":\"").append(escapeJson(rs.getString("committee_name"))).append("\",")
                      .append("\"date\":\"").append(rs.getDate("notice_date")).append("\"")
                      .append("}");
                    first = false;
                }
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    private String getPaymentHistory(Connection conn, int societyId, Integer apartmentId) throws SQLException {
        StringBuilder sb = new StringBuilder("{\"payments\":[");
        if (apartmentId == null || apartmentId <= 0) {
            sb.append("]}");
            return sb.toString();
        }
        String sql = "SELECT mp.payment_id, mp.amount, mp.payment_month, mp.status, " +
                     "mp.mode_of_payment, mp.payment_date, mp.razorpay_payment_id, " +
                     "a.apartment_label " +
                     "FROM maintenance_payment mp " +
                     "JOIN apartment a ON mp.apartment_id = a.apartment_id " +
                     "WHERE mp.society_id=? AND mp.apartment_id=? " +
                     "ORDER BY mp.payment_month DESC";
        boolean first = true;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, societyId);
            stmt.setInt(2, apartmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (!first) sb.append(",");
                    sb.append("{")
                      .append("\"paymentId\":").append(rs.getInt("payment_id")).append(",")
                      .append("\"apartment\":\"").append(escapeJson(rs.getString("apartment_label"))).append("\",")
                      .append("\"amount\":").append(rs.getBigDecimal("amount")).append(",")
                      .append("\"month\":\"").append(escapeJson(rs.getString("payment_month"))).append("\",")
                      .append("\"status\":\"").append(escapeJson(rs.getString("status"))).append("\",")
                      .append("\"mode\":\"").append(escapeJson(rs.getString("mode_of_payment"))).append("\",")
                      .append("\"date\":\"").append(rs.getTimestamp("payment_date") != null ? rs.getTimestamp("payment_date").toString() : "").append("\",")
                      .append("\"razorpayId\":\"").append(escapeJson(rs.getString("razorpay_payment_id"))).append("\"")
                      .append("}");
                    first = false;
                }
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    private String getDashboardData(Connection conn, int societyId, Integer apartmentId) throws SQLException {
        StringBuilder json = new StringBuilder("{");
        // Count of notices
        json.append("\"totalNotices\":").append(getCount(conn,
            "SELECT COUNT(*) FROM notice WHERE society_id=?", societyId)).append(",");
        // Pending complaints for this resident
        int pendingComplaints = 0;
        if (apartmentId != null && apartmentId > 0) {
            String sql = "SELECT COUNT(*) FROM complaint WHERE society_id=? AND apartment_id=? AND status!='RESOLVED'";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, societyId);
                stmt.setInt(2, apartmentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) pendingComplaints = rs.getInt(1);
                }
            }
        }
        json.append("\"pendingComplaints\":").append(pendingComplaints);
        // Get notices for dashboard too
        json.append(",\"notices\":").append(getNoticesArray(conn, societyId));
        json.append("}");
        return json.toString();
    }

    private String getNoticesArray(Connection conn, int societyId) throws SQLException {
        String sql = "SELECT n.notice_id, n.title, n.content, n.notice_date, c.name as committee_name " +
                     "FROM notice n LEFT JOIN committee c ON n.committee_id = c.committee_id " +
                     "WHERE n.society_id=? ORDER BY n.notice_date DESC LIMIT 5";
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, societyId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (!first) sb.append(",");
                    sb.append("{")
                      .append("\"title\":\"").append(escapeJson(rs.getString("title"))).append("\",")
                      .append("\"content\":\"").append(escapeJson(rs.getString("content"))).append("\",")
                      .append("\"committee\":\"").append(escapeJson(rs.getString("committee_name"))).append("\",")
                      .append("\"date\":\"").append(rs.getDate("notice_date")).append("\"")
                      .append("}");
                    first = false;
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private int getCount(Connection conn, String query, int societyId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, societyId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private String getComplaints(Connection conn, int societyId, Integer apartmentId) throws SQLException {
        StringBuilder sb = new StringBuilder("{\"complaints\":[");
        if (apartmentId == null || apartmentId <= 0) {
            sb.append("]}");
            return sb.toString();
        }
        String sql = "SELECT complaint_id, description, date_filed, status, resolved_date " +
                     "FROM complaint WHERE society_id=? AND apartment_id=? ORDER BY date_filed DESC";
        boolean first = true;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, societyId);
            stmt.setInt(2, apartmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (!first) sb.append(",");
                    sb.append("{")
                      .append("\"id\":").append(rs.getInt("complaint_id")).append(",")
                      .append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\",")
                      .append("\"date\":\"").append(rs.getDate("date_filed")).append("\",")
                      .append("\"status\":\"").append(escapeJson(rs.getString("status"))).append("\",")
                      .append("\"resolvedDate\":\"").append(rs.getDate("resolved_date") != null ? rs.getDate("resolved_date").toString() : "").append("\"")
                      .append("}");
                    first = false;
                }
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"")
                     .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
