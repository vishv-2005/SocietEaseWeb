package storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database connection manager using environment variables.
 * 
 * Environment variables:
 *   DB_URL      - JDBC URL (default: jdbc:mysql://localhost:3306/societease)
 *   DB_USER     - Database username (default: root)
 *   DB_PASSWORD  - Database password (default: empty)
 * 
 * For production, always set these environment variables.
 * Connection pooling is handled via simple connection caching for the servlet container.
 */
public class DBConnector {

    private static final Logger LOGGER = Logger.getLogger(DBConnector.class.getName());

    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static boolean initialized = false;

    static {
        initialize();
    }

    private static void initialize() {
        // Read from environment variables with sensible defaults for local dev
        DB_URL = getEnvOrDefault("DB_URL",
                "jdbc:mysql://localhost:3306/societease?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8");
        DB_USER = getEnvOrDefault("DB_USER", "root");
        DB_PASSWORD = getEnvOrDefault("DB_PASSWORD", "d4d5Nf3Nf6Bf4");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            LOGGER.info("MySQL JDBC driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found!", e);
        }
        initialized = true;
    }

    /**
     * Gets a connection to the societease database.
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) initialize();
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Gets a connection to MySQL server without specifying a database.
     * Used only during initial schema creation.
     */
    public static Connection getServerConnection() throws SQLException {
        if (!initialized) initialize();
        String serverUrl = getEnvOrDefault("DB_SERVER_URL",
                "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8");
        return DriverManager.getConnection(serverUrl, DB_USER, DB_PASSWORD);
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        // Also check system properties (useful for IDE run configurations)
        value = System.getProperty(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        if (!"DB_PASSWORD".equals(key)) {
            LOGGER.info(key + " not set, using default: " + defaultValue);
        }
        return defaultValue;
    }
}
