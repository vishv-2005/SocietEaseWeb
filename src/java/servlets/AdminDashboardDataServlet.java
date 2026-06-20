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
 * Serves admin dashboard data as JSON (multi-tenant).
 * Also handles the initial dashboard page render when ?view=dashboard is passed.
 */
@WebServlet("/adminDashboardData")
public class AdminDashboardDataServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminDashboardDataServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("societyId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int societyId = (int) session.getAttribute("societyId");

        // If ?view=dashboard, forward to the JSP page
        if ("dashboard".equals(request.getParameter("view"))) {
            request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
            return;
        }

        // Otherwise return JSON data
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = DBConnector.getConnection()) {
            StringBuilder json = new StringBuilder("{");

            // Total apartments (occupied)
            json.append("\"totalResidents\":").append(getCount(conn,
                "SELECT COUNT(*) FROM apartment WHERE society_id=? AND status != 'VACANT'", societyId)).append(",");

            // Total apartments
            json.append("\"totalApartments\":").append(getCount(conn,
                "SELECT COUNT(*) FROM apartment WHERE society_id=?", societyId)).append(",");

            // Active complaints
            json.append("\"activeComplaints\":").append(getCount(conn,
                "SELECT COUNT(*) FROM complaint WHERE society_id=? AND status='PENDING'", societyId)).append(",");

            // Total vehicles
            json.append("\"totalVehicles\":").append(getCount(conn,
                "SELECT COUNT(*) FROM vehicle WHERE society_id=?", societyId)).append(",");

            // Total helpers
            json.append("\"totalHelpers\":").append(getCount(conn,
                "SELECT COUNT(*) FROM helper WHERE society_id=? AND is_active=TRUE", societyId)).append(",");

            // Recent notices
            json.append("\"notices\":").append(getNotices(conn, societyId)).append(",");

            // Active committees
            json.append("\"committees\":").append(getCommittees(conn, societyId));

            json.append("}");

            PrintWriter out = response.getWriter();
            out.print(json.toString());
            out.flush();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Dashboard data error", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = response.getWriter();
            out.print("{\"error\":\"An error occurred loading dashboard data.\"}");
            out.flush();
        }
    }

    private int getCount(Connection conn, String query, int societyId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, societyId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private String getNotices(Connection conn, int societyId) throws SQLException {
        String sql = "SELECT n.notice_id, n.title, n.content, n.notice_date, c.name as committee_name " +
                     "FROM notice n LEFT JOIN committee c ON n.committee_id = c.committee_id " +
                     "WHERE n.society_id=? ORDER BY n.notice_date DESC LIMIT 7";
        StringBuilder sb = new StringBuilder("[");
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
        sb.append("]");
        return sb.toString();
    }

    private String getCommittees(Connection conn, int societyId) throws SQLException {
        String sql = "SELECT c.committee_id, c.name, c.description FROM committee WHERE c.society_id=?";
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, societyId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (!first) sb.append(",");
                    sb.append("{")
                      .append("\"committee_id\":").append(rs.getInt("committee_id")).append(",")
                      .append("\"name\":\"").append(escapeJson(rs.getString("name"))).append("\",")
                      .append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\"")
                      .append("}");
                    first = false;
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"")
                     .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
