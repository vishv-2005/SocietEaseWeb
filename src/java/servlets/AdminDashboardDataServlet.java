package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import storage.DBConnector;

@WebServlet("/adminDashboardData")
public class AdminDashboardDataServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = DBConnector.getConnection()) {
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{");

            // Get total residents count
            jsonResponse.append("\"totalResidents\":").append(getTotalResidents(conn)).append(",");

            // Get active complaints count (status = 'Pending')
            jsonResponse.append("\"activeComplaints\":").append(getActiveComplaints(conn)).append(",");

            // Get total vehicles count
            jsonResponse.append("\"totalVehicles\":").append(getTotalVehicles(conn)).append(",");

            // Get recent notices
            jsonResponse.append("\"notices\":").append(getRecentNotices(conn)).append(",");

            // Get active committees
            jsonResponse.append("\"committees\":").append(getActiveCommittees(conn));

            jsonResponse.append("}");

            PrintWriter out = response.getWriter();
            out.print(jsonResponse.toString());
            out.flush();

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = response.getWriter();
            out.print("{\"error\": \"Database error: " + escapeJson(e.getMessage()) + "\"}");
            out.flush();
        }
    }

    private int getTotalResidents(Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) FROM resident WHERE name IS NOT NULL";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private int getActiveComplaints(Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) FROM complaint WHERE status = 'Pending'";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private int getTotalVehicles(Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) FROM vehicle";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private String getRecentNotices(Connection conn) throws SQLException {
        String query = "SELECT noticeID, content, issuedBy, date FROM notice ORDER BY date DESC LIMIT 7";
        StringBuilder notices = new StringBuilder("[");
        boolean first = true;

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                if (!first) {
                    notices.append(",");
                }
                notices.append("{")
                      .append("\"notice_id\":").append(rs.getInt("noticeID")).append(",")
                      .append("\"content\":\"").append(escapeJson(rs.getString("content"))).append("\",")
                      .append("\"issued_by\":\"").append(escapeJson(String.valueOf(rs.getObject("issuedBy")))).append("\",")
                      .append("\"date\":\"").append(rs.getDate("date")).append("\"")
                      .append("}");
                first = false;
            }
        }
        notices.append("]");
        return notices.toString();
    }

    private String getActiveCommittees(Connection conn) throws SQLException {
        String query = "SELECT committeeID, name, description, head, apartmentNumber FROM committee";
        StringBuilder committees = new StringBuilder("[");
        boolean first = true;

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                if (!first) {
                    committees.append(",");
                }
                committees.append("{")
                        .append("\"committee_id\":").append(rs.getInt("committeeID")).append(",")
                        .append("\"name\":\"").append(escapeJson(rs.getString("name"))).append("\",")
                        .append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\",")
                        .append("\"head_name\":\"").append(escapeJson(rs.getString("head"))).append("\",")
                        .append("\"apartment_number\":\"").append(escapeJson(String.valueOf(rs.getObject("apartmentNumber")))).append("\"")
                        .append("}");
                first = false;
            }
        }
        committees.append("]");
        return committees.toString();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
