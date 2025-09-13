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

@WebServlet("/ManageVehiclesServlet")
public class ManageVehiclesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Map<String, Object>> vehicles = new ArrayList<>();
        List<Map<String, Object>> residents = new ArrayList<>();
        
        try (Connection conn = DBConnector.getConnection()) {
            // Fetch all vehicles with resident details
            String sql = "SELECT v.*, r.name as residentName, r.apartmentNumber " +
                        "FROM vehicle v " +
                        "LEFT JOIN resident r ON v.apartmentNumber = r.apartmentNumber " +
                        "ORDER BY v.number";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> vehicle = new HashMap<>();
                    vehicle.put("number", rs.getString("number"));
                    vehicle.put("type", rs.getString("type"));
                    vehicle.put("residentName", rs.getString("residentName"));
                    vehicle.put("apartmentNumber", rs.getInt("apartmentNumber"));
                    vehicles.add(vehicle);
                }
            }

            // Fetch all residents for the dropdown
            String residentSql = "SELECT apartmentNumber, name FROM resident WHERE name IS NOT NULL ORDER BY apartmentNumber";
            try (PreparedStatement stmt = conn.prepareStatement(residentSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> resident = new HashMap<>();
                    resident.put("apartmentNumber", rs.getInt("apartmentNumber"));
                    resident.put("name", rs.getString("name"));
                    residents.add(resident);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        
        request.setAttribute("vehicles", vehicles);
        request.setAttribute("residents", residents);
        request.getRequestDispatcher("manageVehicles.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        
        try (Connection conn = DBConnector.getConnection()) {
            if ("add".equals(action)) {
                addVehicle(conn, request);
            } else if ("remove".equals(action)) {
                removeVehicle(conn, request);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        
        response.sendRedirect("ManageVehiclesServlet");
    }

    private void addVehicle(Connection conn, HttpServletRequest request) throws SQLException {
        String number = request.getParameter("number");
        String type = request.getParameter("type");
        int apartmentNumber = Integer.parseInt(request.getParameter("apartmentNumber"));

        String sql = "INSERT INTO vehicle (number, type, apartmentNumber) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, number);
            stmt.setString(2, type);
            stmt.setInt(3, apartmentNumber);
            stmt.executeUpdate();
        }
    }

    private void removeVehicle(Connection conn, HttpServletRequest request) throws SQLException {
        String number = request.getParameter("number");
        
        String sql = "DELETE FROM vehicle WHERE number = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, number);
            stmt.executeUpdate();
        }
    }
} 