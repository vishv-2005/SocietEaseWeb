package servlets;

import storage.DBConnector;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/IssueNoticeServlet")
public class IssueNoticeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Map<String, Object>> committees = new ArrayList<>();
        
        try (Connection conn = DBConnector.getConnection()) {
            // Fetch all committees for the dropdown
            String sql = "SELECT c.*, r.name as headName FROM committee c " +
                        "LEFT JOIN resident r ON c.apartmentNumber = r.apartmentNumber " +
                        "ORDER BY c.name";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> committee = new HashMap<>();
                    committee.put("committeeID", rs.getInt("committeeID"));
                    committee.put("name", rs.getString("name"));
                    committee.put("headName", rs.getString("headName"));
                    committees.add(committee);
                }
            }
            
            // Set current date for the date input
            request.setAttribute("currentDate", new java.sql.Date(System.currentTimeMillis()));
            request.setAttribute("committees", committees);
            request.getRequestDispatcher("issueNotice.jsp").forward(request, response);
            
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String issueDate = request.getParameter("issueDate");
        int issuedBy = Integer.parseInt(request.getParameter("issuedBy"));
        
        try (Connection conn = DBConnector.getConnection()) {
            String sql = "INSERT INTO notice (date, title, content, issuedBy) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, java.sql.Date.valueOf(issueDate));
                stmt.setString(2, title);
                stmt.setString(3, content);
                stmt.setInt(4, issuedBy);
                stmt.executeUpdate();
            }
            
            request.getSession().setAttribute("message", "Notice issued successfully!");
            request.getSession().setAttribute("messageType", "success");
            
        } catch (SQLException e) {
            request.getSession().setAttribute("message", "Error issuing notice: " + e.getMessage());
            request.getSession().setAttribute("messageType", "danger");
        }
        
        response.sendRedirect("IssueNoticeServlet");
    }
} 