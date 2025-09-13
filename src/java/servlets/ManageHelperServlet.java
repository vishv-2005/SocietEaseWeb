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

@WebServlet("/ManageHelperServlet")
public class ManageHelperServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Map<String, Object>> helpers = new ArrayList<>();
        List<Integer> apartments = new ArrayList<>();
        try (Connection conn = DBConnector.getConnection()) {
            // Fetch all apartments
            String aptSql = "SELECT apartmentNumber FROM apartment ORDER BY apartmentNumber";
            try (PreparedStatement stmt = conn.prepareStatement(aptSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    apartments.add(rs.getInt("apartmentNumber"));
                }
            }
            // Fetch all helpers
            String helperSql = "SELECT * FROM helper ORDER BY name";
            try (PreparedStatement stmt = conn.prepareStatement(helperSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> helper = new HashMap<>();
                    int helperID = rs.getInt("helperID");
                    helper.put("helperID", helperID);
                    helper.put("name", rs.getString("name"));
                    helper.put("role", rs.getString("role"));
                    helper.put("aadharNumber", rs.getString("aadharNumber"));
                    helper.put("contactInformation", rs.getString("contactInformation"));
                    helper.put("salary", rs.getBigDecimal("salary"));
                    // Fetch assigned apartments
                    List<Integer> assignedApts = new ArrayList<>();
                    String assignSql = "SELECT apartmentNumber FROM apartment_helper WHERE helperID=? ORDER BY apartmentNumber";
                    try (PreparedStatement assignStmt = conn.prepareStatement(assignSql)) {
                        assignStmt.setInt(1, helperID);
                        try (ResultSet assignRs = assignStmt.executeQuery()) {
                            while (assignRs.next()) {
                                assignedApts.add(assignRs.getInt("apartmentNumber"));
                            }
                        }
                    }
                    helper.put("apartments", assignedApts);
                    helpers.add(helper);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        request.setAttribute("helpers", helpers);
        request.setAttribute("apartments", apartments);
        request.getRequestDispatcher("manageHelpers.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        try (Connection conn = DBConnector.getConnection()) {
            if ("add".equals(action)) {
                String name = request.getParameter("name");
                String role = request.getParameter("role");
                String aadhar = request.getParameter("aadharNumber");
                String contact = request.getParameter("contactInformation");
                String salary = request.getParameter("salary");
                String sql = "INSERT INTO helper (name, role, aadharNumber, contactInformation, salary) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, name);
                    stmt.setString(2, role);
                    stmt.setString(3, aadhar);
                    stmt.setString(4, contact);
                    stmt.setBigDecimal(5, new java.math.BigDecimal(salary));
                    stmt.executeUpdate();
                }
            } else if ("assign".equals(action)) {
                int helperID = Integer.parseInt(request.getParameter("helperID"));
                int apartmentNumber = Integer.parseInt(request.getParameter("apartmentNumber"));
                String sql = "INSERT INTO apartment_helper (apartmentNumber, helperID) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, apartmentNumber);
                    stmt.setInt(2, helperID);
                    stmt.executeUpdate();
                }
            } else if ("unassign".equals(action)) {
                int helperID = Integer.parseInt(request.getParameter("helperID"));
                int apartmentNumber = Integer.parseInt(request.getParameter("apartmentNumber"));
                String sql = "DELETE FROM apartment_helper WHERE apartmentNumber=? AND helperID=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, apartmentNumber);
                    stmt.setInt(2, helperID);
                    stmt.executeUpdate();
                }
            } else if ("remove".equals(action)) {
                int helperID = Integer.parseInt(request.getParameter("helperID"));
                // Remove from apartment_helper first (if not ON DELETE CASCADE)
                String delAssign = "DELETE FROM apartment_helper WHERE helperID=?";
                try (PreparedStatement stmt = conn.prepareStatement(delAssign)) {
                    stmt.setInt(1, helperID);
                    stmt.executeUpdate();
                }
                // Remove from helper
                String delHelper = "DELETE FROM helper WHERE helperID=?";
                try (PreparedStatement stmt = conn.prepareStatement(delHelper)) {
                    stmt.setInt(1, helperID);
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        response.sendRedirect("ManageHelperServlet");
    }
} 