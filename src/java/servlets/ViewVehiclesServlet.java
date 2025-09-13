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

@WebServlet("/ViewVehiclesServlet")
public class ViewVehiclesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Map<String, Object>> vehicles = new ArrayList<>();
        try (Connection conn = DBConnector.getConnection()) {
            String sql = "SELECT number, type, owner, apartmentNumber FROM vehicle ORDER BY apartmentNumber, number";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> vehicle = new HashMap<>();
                    vehicle.put("number", rs.getString("number"));
                    vehicle.put("type", rs.getString("type"));
                    vehicle.put("owner", rs.getString("owner"));
                    vehicle.put("apartmentNumber", rs.getInt("apartmentNumber"));
                    vehicles.add(vehicle);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        request.setAttribute("vehicles", vehicles);
        request.getRequestDispatcher("viewVehicles.jsp").forward(request, response);
    }
} 