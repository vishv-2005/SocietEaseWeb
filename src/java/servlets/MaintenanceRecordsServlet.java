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

@WebServlet("/MaintenanceRecordsServlet")
public class MaintenanceRecordsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Map<String, Object>> records = new ArrayList<>();
        try (Connection conn = DBConnector.getConnection()) {
            String sql = "SELECT m.*, r.name as residentName FROM maintenance m " +
                        "LEFT JOIN resident r ON m.apartmentNumber = r.apartmentNumber " +
                        "ORDER BY m.paymentDate DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("maintenanceID", rs.getInt("maintenanceID"));
                    record.put("name", rs.getString("name"));
                    record.put("apartmentNumber", rs.getInt("apartmentNumber"));
                    record.put("contactInformation", rs.getString("contactInformation"));
                    record.put("amountPaid", rs.getBigDecimal("amountPaid"));
                    record.put("modeOfPayment", rs.getString("modeOfPayment"));
                    record.put("paymentDate", rs.getDate("paymentDate"));
                    record.put("residentName", rs.getString("residentName"));
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        request.setAttribute("records", records);
        request.getRequestDispatcher("maintenanceRecords.jsp").forward(request, response);
    }
} 