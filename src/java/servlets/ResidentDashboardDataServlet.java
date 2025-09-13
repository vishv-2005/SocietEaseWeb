package servlets;

import storage.DBConnector;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/residentDashboardData")
public class ResidentDashboardDataServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = DBConnector.getConnection()) {
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{");

            // Get recent notices
            jsonResponse.append("\"notices\":").append(getRecentNotices(conn)).append(",");

            // Get helpers
            jsonResponse.append("\"helpers\":").append(getHelpers(conn)).append(",");

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

    private String getHelpers(Connection conn) throws SQLException {
        String query = "SELECT helperID, name, role, salary, contactInformation, aadharNumber FROM helper ORDER BY helperID";
        StringBuilder helpers = new StringBuilder("[");
        boolean first = true;
        try (PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                if (!first) {
                    helpers.append(",");
                }
                helpers.append("{")
                        .append("\"helper_id\":").append(rs.getInt("helperID")).append(",")
                        .append("\"name\":\"").append(escapeJson(rs.getString("name"))).append("\",")
                        .append("\"role\":\"").append(escapeJson(rs.getString("role"))).append("\",")
                        .append("\"salary\":").append(rs.getInt("salary")).append(",")
                        .append("\"contact\":\"").append(escapeJson(rs.getString("contactInformation"))).append("\",")
                        .append("\"aadharNumber\":\"").append(escapeJson(rs.getString("aadharNumber"))).append("\"")
                        .append("}");
                first = false;
            }
        }
        helpers.append("]");
        return helpers.toString();
    }

    private String getActiveCommittees(Connection conn) throws SQLException {
        String query = "SELECT committeeID, name, description, head, apartmentNumber FROM committee ORDER BY name";
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
                        .append("\"head\":\"").append(escapeJson(rs.getString("head"))).append("\",")
                        .append("\"apartment_number\":").append(rs.getInt("apartmentNumber"))
                        .append("}");
                first = false;
            }
        }
        committees.append("]");
        return committees.toString();
    }

    private String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
