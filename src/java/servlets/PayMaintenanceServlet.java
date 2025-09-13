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

@WebServlet("/payMaintenance")
public class PayMaintenanceServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Map<String, Object>> residents = new ArrayList<>();
        try (Connection conn = DBConnector.getConnection()) {
            String sql = "SELECT apartmentNumber, name, contactInformation FROM resident WHERE name IS NOT NULL ORDER BY apartmentNumber";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> resident = new HashMap<>();
                    resident.put("apartmentNumber", rs.getInt("apartmentNumber"));
                    resident.put("name", rs.getString("name"));
                    resident.put("contactInformation", rs.getString("contactInformation"));
                    residents.add(resident);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        request.setAttribute("residents", residents);
        request.getRequestDispatcher("payMaintenance.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String apartment = request.getParameter("apartment");
        String amount = request.getParameter("amount");
        String mode = request.getParameter("mode");
        String date = request.getParameter("date");
        String contact = request.getParameter("contact");

        try (Connection conn = DBConnector.getConnection()) {
            // Fetch resident name
            String name = null;
            String nameSql = "SELECT name FROM resident WHERE apartmentNumber = ?";
            try (PreparedStatement nameStmt = conn.prepareStatement(nameSql)) {
                nameStmt.setInt(1, Integer.parseInt(apartment));
                try (ResultSet rs = nameStmt.executeQuery()) {
                    if (rs.next()) {
                        name = rs.getString("name");
                    }
                }
            }

            // Insert maintenance record with name
            String sql = "INSERT INTO maintenance (name, apartmentNumber, contactInformation, amountPaid, modeOfPayment, paymentDate) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setInt(2, Integer.parseInt(apartment));
                stmt.setString(3, contact);
                stmt.setDouble(4, Double.parseDouble(amount));
                stmt.setString(5, mode);
                stmt.setDate(6, java.sql.Date.valueOf(date));
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            // Optionally handle error
        }
        response.sendRedirect("payMaintenance");
    }
} 