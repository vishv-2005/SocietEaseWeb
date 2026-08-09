package storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database connection manager using environment variables.
 *
 * Environment variables (all read at startup):
 *   DB_HOST     - MySQL host     (default: societease-db-societeaseweb.c.aivencloud.com)
 *   DB_PORT     - MySQL port     (default: 24410)
 *   DB_NAME     - Database name  (default: defaultdb)
 *   DB_USER     - Username       (default: avnadmin)
 *   DB_PASSWORD - Password       (NO default — MUST be set via environment)
 *
 * SSL note: The connection uses useSSL=true & requireSSL=true with
 * verifyServerCertificate=false.  This encrypts the wire protocol but does NOT
 * verify the server's identity (no CA certificate imported into the Java trust
 * store).  This is an acceptable tradeoff for a student/personal project but
 * should be hardened for production by importing Aiven's CA cert and setting
 * verifyServerCertificate=true.
 */
public class DBConnector {

    private static final Logger LOGGER = Logger.getLogger(DBConnector.class.getName());

    private static String dbHost;
    private static String dbPort;
    private static String dbName;
    private static String dbUser;
    private static String dbPassword;
    private static boolean initialized = false;

    static {
        initialize();
    }

    private static void initialize() {
        dbHost = getEnvOrDefault("DB_HOST", "societease-db-societeaseweb.c.aivencloud.com");
        dbPort = getEnvOrDefault("DB_PORT", "24410");
        dbName = getEnvOrDefault("DB_NAME", "defaultdb");
        dbUser = getEnvOrDefault("DB_USER", "avnadmin");

        // Password MUST come from the environment — no hardcoded fallback.
        dbPassword = getEnvOrDefault("DB_PASSWORD", null);
        if (dbPassword == null || dbPassword.isEmpty()) {
            LOGGER.severe("DB_PASSWORD is not set! Database connections will fail. "
                    + "Set the DB_PASSWORD environment variable (or -DDB_PASSWORD system property).");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            LOGGER.info("MySQL JDBC driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found!", e);
        }
        initialized = true;

        LOGGER.info("DB target: " + dbHost + ":" + dbPort + "/" + dbName + " (user=" + dbUser + ")");
    }

    /**
     * Builds the JDBC URL for a given database name (or empty string for server-level connection).
     */
    private static String buildJdbcUrl(String database) {
        return "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + database
                + "?useSSL=true"
                + "&requireSSL=true"
                + "&verifyServerCertificate=false"
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=UTC"
                + "&characterEncoding=UTF-8";
    }

    /**
     * Gets a connection to the application database (DB_NAME).
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) initialize();
        return DriverManager.getConnection(buildJdbcUrl(dbName), dbUser, dbPassword);
    }

    /**
     * Gets a connection to MySQL server without specifying a database.
     * Used only during initial schema creation (CREATE DATABASE IF NOT EXISTS).
     */
    public static Connection getServerConnection() throws SQLException {
        if (!initialized) initialize();
        return DriverManager.getConnection(buildJdbcUrl(""), dbUser, dbPassword);
    }

    /**
     * Returns the configured database name (read from DB_NAME env var).
     * Used by AppStartupListener for CREATE DATABASE IF NOT EXISTS.
     */
    public static String getDatabaseName() {
        if (!initialized) initialize();
        return dbName;
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
        if (!"DB_PASSWORD".equals(key) && defaultValue != null) {
            LOGGER.info(key + " not set, using default: " + defaultValue);
        }
        return defaultValue;
    }
}
