package servlets;

import storage.DBConnector;
import storage.DatabaseInitializer;
import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name = "mysqlServlet", urlPatterns = {"/mysqlServlet"})
public class mysqlServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8"); // Set content type for the response

        // Retrieve DB credentials (username and password) from the form
        String dbUser = request.getParameter("dbuser");
        String dbPass = request.getParameter("dbpass");

        if (dbUser != null && dbPass != null) {
            DBConnector.setCredentials(dbUser, dbPass);
            try (Connection serverConn = DBConnector.getServerConnection()) {
                // Create database if it doesn't exist
                Statement stmt = serverConn.createStatement();
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS societease_hvt");
                stmt.close();
            } catch (SQLException e) {
                System.err.println("❌ Database creation failed: " + e.getMessage());
                request.setAttribute("message", "Database creation failed! Error: " + e.getMessage());
                request.getRequestDispatcher("mysql.jsp").forward(request, response);
                return;
            }
            try (Connection connection = DBConnector.getConnection()) {
                // Initialize database and tables
                DatabaseInitializer initializer = new DatabaseInitializer(connection);
                initializer.initializeDatabase();

                // If everything is successful, redirect to index.jsp
                System.out.println("Database initialized successfully!");
                request.setAttribute("message", "Database Initialized Successfully!");
                response.sendRedirect("index.jsp");
                return;

            } catch (SQLException e) {
                System.err.println("❌ Connection Failed: " + e.getMessage());
                // If connection or initialization fails, show an error message on mysql.jsp
                request.setAttribute("message", "Connection Failed! Error: " + e.getMessage());
                request.getRequestDispatcher("mysql.jsp").forward(request, response);
            }
        } else {
            // If DB credentials are missing, ask for both username and password
            request.setAttribute("message", "Please enter both DB Username and Password!");
            request.getRequestDispatcher("mysql.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response); // Handle GET requests by calling doPost
    }
}
