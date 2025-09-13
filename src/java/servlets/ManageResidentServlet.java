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

@WebServlet("/ManageResidentServlet")
public class ManageResidentServlet extends HttpServlet {
    private static final List<Integer> APARTMENTS = Arrays.asList(
        101, 102, 103, 104,
        201, 202, 203, 204,
        301, 302, 303, 304,
        401, 402, 403, 404,
        501, 502, 503, 504
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Map<String, Object>> residents = new ArrayList<>();
        try (Connection conn = DBConnector.getConnection()) {
            for (int apt : APARTMENTS) {
                Map<String, Object> res = new HashMap<>();
                res.put("apartmentNumber", apt);
                String sql = "SELECT r.name, r.contactInformation, r.email, a.type FROM resident r JOIN apartment a ON r.apartmentNumber = a.apartmentNumber WHERE r.apartmentNumber = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, apt);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            res.put("name", rs.getString("name"));
                            res.put("contactInformation", rs.getString("contactInformation"));
                            res.put("email", rs.getString("email"));
                            res.put("type", rs.getString("type"));
                        }
                    }
                }
                residents.add(res);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        request.setAttribute("residents", residents);
        request.getRequestDispatcher("manageResidents.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        int apartmentNumber = Integer.parseInt(request.getParameter("apartmentNumber"));
        try (Connection conn = DBConnector.getConnection()) {
            if ("add".equals(action)) {
                String name = request.getParameter("name");
                String contact = request.getParameter("contactInformation");
                String email = request.getParameter("email");
                String type = request.getParameter("type");
                String sql = "UPDATE resident SET name=?, contactInformation=?, email=? WHERE apartmentNumber=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, name);
                    stmt.setString(2, contact);
                    stmt.setString(3, email);
                    stmt.setInt(4, apartmentNumber);
                    stmt.executeUpdate();
                }
                String sql2 = "UPDATE apartment SET type=? WHERE apartmentNumber=?";
                try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                    stmt2.setString(1, type);
                    stmt2.setInt(2, apartmentNumber);
                    stmt2.executeUpdate();
                }
            } else if ("update".equals(action)) {
                String name = request.getParameter("name");
                String contact = request.getParameter("contactInformation");
                String email = request.getParameter("email");
                String type = request.getParameter("type");
                String sql = "UPDATE resident SET name=?, contactInformation=?, email=? WHERE apartmentNumber=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, name);
                    stmt.setString(2, contact);
                    stmt.setString(3, email);
                    stmt.setInt(4, apartmentNumber);
                    stmt.executeUpdate();
                }
                String sql2 = "UPDATE apartment SET type=? WHERE apartmentNumber=?";
                try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                    stmt2.setString(1, type);
                    stmt2.setInt(2, apartmentNumber);
                    stmt2.executeUpdate();
                }
            } else if ("remove".equals(action)) {
                String sql = "UPDATE resident SET name=NULL, contactInformation=NULL, email=NULL WHERE apartmentNumber=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, apartmentNumber);
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        response.sendRedirect("ManageResidentServlet");
    }
} 