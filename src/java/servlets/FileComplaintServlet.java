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

@WebServlet("/fileComplaint")
public class FileComplaintServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Map<String, Object>> apartments = new ArrayList<>();
        try (Connection conn = DBConnector.getConnection()) {
            String sql = "SELECT apartmentNumber FROM resident WHERE name IS NOT NULL ORDER BY apartmentNumber";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> apt = new HashMap<>();
                    apt.put("apartmentNumber", rs.getInt("apartmentNumber"));
                    apartments.add(apt);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        request.setAttribute("apartments", apartments);
        request.getRequestDispatcher("fileComplaint.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String apartment = request.getParameter("apartment");
        String content = request.getParameter("content");
        String date = request.getParameter("date");
        try (Connection conn = DBConnector.getConnection()) {
            String sql = "INSERT INTO complaint (apartmentNumber, description, dateFiled, status) VALUES (?, ?, ?, 'Pending')";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, Integer.parseInt(apartment));
                stmt.setString(2, content);
                stmt.setDate(3, java.sql.Date.valueOf(date));
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            // Optionally handle error
        }
        response.sendRedirect("fileComplaint");
    }
} 