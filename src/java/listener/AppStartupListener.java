package listener;

import storage.DBConnector;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application startup listener.
 * Automatically creates the database and all tables when the application deploys.
 * Replaces the old mysql.jsp manual initialization.
 */
@WebListener
public class AppStartupListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AppStartupListener.class.getName());
    private static final String DB_NAME = "societease";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("=== SocietEase Application Starting ===");
        try {
            createDatabaseIfNotExists();
            createTablesIfNotExist();
            LOGGER.info("=== Database initialization complete ===");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "FATAL: Database initialization failed!", e);
            throw new RuntimeException("Application startup failed — database not available.", e);
        }
    }

    private void createDatabaseIfNotExists() throws SQLException {
        try (Connection conn = DBConnector.getServerConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME +
                " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            LOGGER.info("Database '" + DB_NAME + "' ensured.");
        }
    }

    private void createTablesIfNotExist() throws SQLException {
        try (Connection conn = DBConnector.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Society
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS society (" +
                "  society_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  name VARCHAR(200) NOT NULL," +
                "  address TEXT NOT NULL," +
                "  city VARCHAR(100) NOT NULL," +
                "  state VARCHAR(100) NOT NULL," +
                "  pincode VARCHAR(10) NOT NULL," +
                "  total_towers INT NOT NULL," +
                "  floors_per_tower INT NOT NULL," +
                "  units_per_floor INT NOT NULL," +
                "  maintenance_amount DECIMAL(10,2) NOT NULL DEFAULT 1000.00," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  is_active BOOLEAN DEFAULT TRUE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            LOGGER.info("Table 'society' ready.");

            // 2. User (authentication)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS user (" +
                "  user_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  society_id INT NOT NULL," +
                "  email VARCHAR(255) NOT NULL UNIQUE," +
                "  password VARCHAR(255) NOT NULL," +
                "  role ENUM('SUPER_ADMIN', 'RP', 'RESIDENT') NOT NULL," +
                "  full_name VARCHAR(200)," +
                "  is_active BOOLEAN DEFAULT TRUE," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (society_id) REFERENCES society(society_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            LOGGER.info("Table 'user' ready.");

            // 3. Tower
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS tower (" +
                "  tower_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  society_id INT NOT NULL," +
                "  tower_name VARCHAR(50) NOT NULL," +
                "  total_floors INT NOT NULL," +
                "  units_per_floor INT NOT NULL," +
                "  FOREIGN KEY (society_id) REFERENCES society(society_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            LOGGER.info("Table 'tower' ready.");

            // 4. Apartment
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS apartment (" +
                "  apartment_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  society_id INT NOT NULL," +
                "  tower_id INT NOT NULL," +
                "  floor_number INT NOT NULL," +
                "  unit_number INT NOT NULL," +
                "  apartment_label VARCHAR(20) NOT NULL," +
                "  status ENUM('VACANT', 'OWNER_OCCUPIED', 'TENANT_OCCUPIED') DEFAULT 'VACANT'," +
                "  UNIQUE KEY unique_apt (society_id, tower_id, floor_number, unit_number)," +
                "  FOREIGN KEY (society_id) REFERENCES society(society_id) ON DELETE CASCADE," +
                "  FOREIGN KEY (tower_id) REFERENCES tower(tower_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            LOGGER.info("Table 'apartment' ready.");

            // 5. Resident (encrypted PII)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS resident (" +
                "  resident_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  society_id INT NOT NULL," +
                "  apartment_id INT NOT NULL," +
                "  user_id INT," +
                "  name_encrypted VARBINARY(512) NOT NULL," +
                "  phone_encrypted VARBINARY(512) NOT NULL," +
                "  email_encrypted VARBINARY(512) NOT NULL," +
                "  resident_type ENUM('OWNER', 'TENANT') NOT NULL," +
                "  move_in_date DATE," +
                "  is_active BOOLEAN DEFAULT TRUE," +
                "  FOREIGN KEY (society_id) REFERENCES society(society_id)," +
                "  FOREIGN KEY (apartment_id) REFERENCES apartment(apartment_id)," +
                "  FOREIGN KEY (user_id) REFERENCES user(user_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            LOGGER.info("Table 'resident' ready.");

            // 6. Helper
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS helper (" +
                "  helper_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  society_id INT NOT NULL," +
                "  name VARCHAR(100) NOT NULL," +
                "  role VARCHAR(50) NOT NULL," +
                "  aadhar_encrypted VARBINARY(512)," +
                "  phone_encrypted VARBINARY(512)," +
                "  salary DECIMAL(10,2)," +
                "  is_active BOOLEAN DEFAULT TRUE," +
                "  FOREIGN KEY (society_id) REFERENCES society(society_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            LOGGER.info("Table 'helper' ready.");

            // 7. Apartment-Helper junction
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS apartment_helper (" +
                "  apartment_id INT NOT NULL," +
                "  helper_id INT NOT NULL," +
                "  PRIMARY KEY (apartment_id, helper_id)," +
                "  FOREIGN KEY (apartment_id) REFERENCES apartment(apartment_id)," +
                "  FOREIGN KEY (helper_id) REFERENCES helper(helper_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            LOGGER.info("Table 'apartment_helper' ready.");

            // 8. Committee
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS committee (" +
                "  committee_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  society_id INT NOT NULL," +
                "  name VARCHAR(100) NOT NULL," +
                "  description TEXT," +
                "  head_resident_id INT," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (society_id) REFERENCES society(society_id)," +
                "  FOREIGN KEY (head_resident_id) REFERENCES resident(resident_id) ON DELETE SET NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            LOGGER.info("Table 'committee' ready.");

            // 9. Notice
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS notice (" +
                "  notice_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  society_id INT NOT NULL," +
                "  committee_id INT," +
                "  title VARCHAR(200) NOT NULL," +
                "  content TEXT NOT NULL," +
                "  notice_date DATE NOT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (society_id) REFERENCES society(society_id)," +
                "  FOREIGN KEY (committee_id) REFERENCES committee(committee_id) ON DELETE SET NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            LOGGER.info("Table 'notice' ready.");

            // 10. Complaint
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS complaint (" +
                "  complaint_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  society_id INT NOT NULL," +
                "  apartment_id INT NOT NULL," +
                "  description TEXT NOT NULL," +
                "  date_filed DATE NOT NULL," +
                "  status ENUM('PENDING', 'IN_PROGRESS', 'RESOLVED') DEFAULT 'PENDING'," +
                "  resolved_date DATE," +
                "  FOREIGN KEY (society_id) REFERENCES society(society_id)," +
                "  FOREIGN KEY (apartment_id) REFERENCES apartment(apartment_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            LOGGER.info("Table 'complaint' ready.");

            // 11. Maintenance Payment
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS maintenance_payment (" +
                "  payment_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  society_id INT NOT NULL," +
                "  apartment_id INT NOT NULL," +
                "  amount DECIMAL(10,2) NOT NULL DEFAULT 1000.00," +
                "  payment_month VARCHAR(7) NOT NULL," +
                "  razorpay_order_id VARCHAR(100)," +
                "  razorpay_payment_id VARCHAR(100)," +
                "  razorpay_signature VARCHAR(255)," +
                "  status ENUM('PENDING', 'PAID', 'FAILED') DEFAULT 'PENDING'," +
                "  mode_of_payment VARCHAR(50)," +
                "  payment_date TIMESTAMP NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  UNIQUE KEY unique_payment (apartment_id, payment_month)," +
                "  FOREIGN KEY (society_id) REFERENCES society(society_id)," +
                "  FOREIGN KEY (apartment_id) REFERENCES apartment(apartment_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            LOGGER.info("Table 'maintenance_payment' ready.");

            // 12. Vehicle
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS vehicle (" +
                "  vehicle_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  society_id INT NOT NULL," +
                "  apartment_id INT NOT NULL," +
                "  number VARCHAR(50) NOT NULL," +
                "  type VARCHAR(20)," +
                "  owner_name VARCHAR(100)," +
                "  FOREIGN KEY (society_id) REFERENCES society(society_id)," +
                "  FOREIGN KEY (apartment_id) REFERENCES apartment(apartment_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            LOGGER.info("Table 'vehicle' ready.");

            // 13. Email Log
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS email_log (" +
                "  log_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  society_id INT NOT NULL," +
                "  recipient VARCHAR(255) NOT NULL," +
                "  subject VARCHAR(255) NOT NULL," +
                "  sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  status ENUM('SENT', 'FAILED') NOT NULL," +
                "  FOREIGN KEY (society_id) REFERENCES society(society_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            LOGGER.info("Table 'email_log' ready.");

            LOGGER.info("All 13 tables verified/created successfully.");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("=== SocietEase Application Shutting Down ===");
    }
}
