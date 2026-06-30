package servlets;

import storage.DBConnector;
import util.InputValidator;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Handles vehicle registration for residents.
 * 
 * GET  → returns vehicles for the logged-in resident (JSON)
 * POST → registers a new vehicle
 */
@WebServlet("/VehicleServlet")
public class VehicleServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(VehicleServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("societyId") == null) {
            out.print("{\"error\":\"Not authenticated\"}");
            return;
        }

        int societyId = (int) session.getAttribute("societyId");
        Integer apartmentId = (Integer) session.getAttribute("apartmentId");

        StringBuilder sb = new StringBuilder("{\"vehicles\":[");
        try (Connection conn = DBConnector.getConnection()) {
            String sql = "SELECT v.vehicle_id, v.vehicle_type, v.registration_number, " +
                         "v.make, v.model, v.color, v.owner_name, v.parking_slot, v.registration_date " +
                         "FROM vehicle v WHERE v.society_id=? AND v.apartment_id=? AND v.is_active=TRUE " +
                         "ORDER BY v.registration_date DESC";
            boolean first = true;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, societyId);
                stmt.setInt(2, apartmentId != null ? apartmentId : 0);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        if (!first) sb.append(",");
                        sb.append("{")
                          .append("\"id\":").append(rs.getInt("vehicle_id")).append(",")
                          .append("\"type\":\"").append(esc(rs.getString("vehicle_type"))).append("\",")
                          .append("\"regNumber\":\"").append(esc(rs.getString("registration_number"))).append("\",")
                          .append("\"make\":\"").append(esc(rs.getString("make"))).append("\",")
                          .append("\"model\":\"").append(esc(rs.getString("model"))).append("\",")
                          .append("\"color\":\"").append(esc(rs.getString("color"))).append("\",")
                          .append("\"owner\":\"").append(esc(rs.getString("owner_name"))).append("\",")
                          .append("\"parking\":\"").append(esc(rs.getString("parking_slot"))).append("\",")
                          .append("\"date\":\"").append(rs.getDate("registration_date")).append("\"")
                          .append("}");
                        first = false;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching vehicles", e);
        }
        sb.append("]}");
        out.print(sb.toString());
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("societyId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int societyId = (int) session.getAttribute("societyId");
        Integer apartmentId = (Integer) session.getAttribute("apartmentId");

        if (apartmentId == null || apartmentId <= 0) {
            session.setAttribute("message", "Your account is not linked to an apartment.");
            session.setAttribute("messageType", "error");
            response.sendRedirect(request.getContextPath() + "/resident/vehicleRegistration.jsp");
            return;
        }

        try {
            String vehicleType = InputValidator.requireNonEmpty(request.getParameter("vehicleType"), "Vehicle Type");
            String regNumber = InputValidator.requireNonEmpty(request.getParameter("regNumber"), "Registration Number").toUpperCase().trim();
            String make = request.getParameter("make") != null ? request.getParameter("make").trim() : "";
            String model = request.getParameter("model") != null ? request.getParameter("model").trim() : "";
            String color = request.getParameter("color") != null ? request.getParameter("color").trim() : "";
            String ownerName = InputValidator.requireNonEmpty(request.getParameter("ownerName"), "Owner Name");
            String parkingSlot = request.getParameter("parkingSlot") != null ? request.getParameter("parkingSlot").trim() : "";

            // Validate vehicle type
            if (!"2_WHEELER".equals(vehicleType) && !"4_WHEELER".equals(vehicleType)) {
                throw new IllegalArgumentException("Invalid vehicle type.");
            }

            try (Connection conn = DBConnector.getConnection()) {
                String sql = "INSERT INTO vehicle (society_id, apartment_id, vehicle_type, registration_number, " +
                             "make, model, color, owner_name, parking_slot, registration_date) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURDATE())";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, societyId);
                    stmt.setInt(2, apartmentId);
                    stmt.setString(3, vehicleType);
                    stmt.setString(4, regNumber);
                    stmt.setString(5, make);
                    stmt.setString(6, model);
                    stmt.setString(7, color);
                    stmt.setString(8, ownerName);
                    stmt.setString(9, parkingSlot);
                    stmt.executeUpdate();
                }
                session.setAttribute("message", "Vehicle registered successfully!");
                session.setAttribute("messageType", "success");
            }

        } catch (IllegalArgumentException e) {
            session.setAttribute("message", e.getMessage());
            session.setAttribute("messageType", "error");
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                session.setAttribute("message", "A vehicle with this registration number already exists in the society.");
            } else {
                session.setAttribute("message", "Failed to register vehicle. Please try again.");
            }
            session.setAttribute("messageType", "error");
            LOGGER.log(Level.SEVERE, "Error registering vehicle", e);
        }

        response.sendRedirect(request.getContextPath() + "/resident/vehicleRegistration.jsp");
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
