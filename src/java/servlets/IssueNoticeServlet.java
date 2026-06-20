package servlets;

import storage.DBConnector;
import util.InputValidator;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Issues notices with multi-tenant support.
 * Bug fixed: column name 'issueDate' → 'notice_date'.
 * Debug System.out.println statements removed.
 */
@WebServlet("/IssueNoticeServlet")
public class IssueNoticeServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(IssueNoticeServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int societyId = (int) session.getAttribute("societyId");

        List<Map<String, Object>> committees = new ArrayList<>();

        try (Connection conn = DBConnector.getConnection()) {
            String sql = "SELECT committee_id, name FROM committee WHERE society_id=? ORDER BY name";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, societyId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> committee = new HashMap<>();
                        committee.put("committeeId", rs.getInt("committee_id"));
                        committee.put("name", rs.getString("name"));
                        committees.add(committee);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading committees for notice form", e);
            session.setAttribute("message", "Error loading committees.");
            session.setAttribute("messageType", "error");
        }

        // Current date
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        request.setAttribute("currentDate", sdf.format(new java.util.Date()));
        request.setAttribute("committees", committees);
        request.getRequestDispatcher("/admin/issueNotice.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int societyId = (int) session.getAttribute("societyId");

        try {
            String title = InputValidator.requireNonEmpty(request.getParameter("title"), "Title");
            String content = InputValidator.requireNonEmpty(request.getParameter("content"), "Content");
            String noticeDate = InputValidator.requireNonEmpty(request.getParameter("noticeDate"), "Date");
            int committeeId = InputValidator.requireInt(request.getParameter("committeeId"), "Committee");

            try (Connection conn = DBConnector.getConnection()) {
                // Fixed: use 'notice_date' not 'issueDate'
                String sql = "INSERT INTO notice (society_id, committee_id, title, content, notice_date) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, societyId);
                    stmt.setInt(2, committeeId);
                    stmt.setString(3, title);
                    stmt.setString(4, content);
                    stmt.setString(5, noticeDate);
                    stmt.executeUpdate();
                }

                session.setAttribute("message", "Notice issued successfully!");
                session.setAttribute("messageType", "success");
                LOGGER.info("Notice issued: " + title + " for society " + societyId);
            }
        } catch (IllegalArgumentException e) {
            session.setAttribute("message", e.getMessage());
            session.setAttribute("messageType", "error");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error issuing notice", e);
            session.setAttribute("message", "Failed to issue notice. Please try again.");
            session.setAttribute("messageType", "error");
        }

        response.sendRedirect("IssueNoticeServlet");
    }
}