package storage;

import java.sql.*;

public class DatabaseInitializer {

    private Connection connection;

    public DatabaseInitializer(Connection connection) {
        this.connection = connection;
    }

    public void initializeDatabase() throws SQLException {
        createDatabase();
        createTables();
        insertSampleData();
    }

    public void useDatabase() throws SQLException {
        createDatabase();
    }

    private void createDatabase() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS societease_hvt");
        stmt.executeUpdate("USE societease_hvt");
        stmt.close();
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            // Create tables
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS resident (name VARCHAR(100), apartmentNumber INT PRIMARY KEY, contactInformation VARCHAR(100), email VARCHAR(100))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS helper (helperID INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, role VARCHAR(50), aadharNumber BIGINT, contactInformation VARCHAR(100), salary DECIMAL(10, 2))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS maintenance (maintenanceID INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), apartmentNumber INT, contactInformation VARCHAR(100), amountPaid DECIMAL(10, 2), modeOfPayment VARCHAR(50), paymentDate DATE, FOREIGN KEY (apartmentNumber) REFERENCES Resident(apartmentNumber))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS complaint (complaintID INT AUTO_INCREMENT PRIMARY KEY, apartmentNumber INT, description TEXT, dateFiled DATE, status VARCHAR(20) DEFAULT 'Pending', FOREIGN KEY (apartmentNumber) REFERENCES Resident(apartmentNumber))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS committee (committeeID INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), description TEXT, head VARCHAR(100), apartmentNumber INT, FOREIGN KEY (apartmentNumber) REFERENCES Resident(apartmentNumber) ON DELETE CASCADE)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS notice (noticeID INT AUTO_INCREMENT PRIMARY KEY, date DATE, title VARCHAR(200), content TEXT, issuedBy INT, FOREIGN KEY (issuedBy) REFERENCES Committee(committeeID))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS vehicle (number VARCHAR(50) PRIMARY KEY, type VARCHAR(20), owner VARCHAR(100), apartmentNumber INT, FOREIGN KEY (apartmentNumber) REFERENCES Resident(apartmentNumber))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS apartment (apartmentNumber INT PRIMARY KEY, type VARCHAR(50), name VARCHAR(100))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS apartment_Helper( apartmentNumber INT, helperID INT, PRIMARY KEY (apartmentNumber, helperID), FOREIGN KEY (apartmentNumber) REFERENCES Apartment(apartmentNumber), FOREIGN KEY (helperID) REFERENCES Helper(helperID) ON DELETE CASCADE)");

            System.out.println("Table created or already exists.");

            stmt.close();
        }
    }

    private void insertSampleData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            
            //resident table
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM resident");
            rs.next();
            int residentCount = rs.getInt(1);

            if (residentCount == 0) {
                stmt.executeUpdate("INSERT INTO resident (name, apartmentNumber, contactInformation, email) VALUES "
                        + "('Ravi Sharma', 101, '9876543210', 'ravi.sharma@gmail.com'),"
                        + "('Neha Patel', 102, '9865432109', 'neha.patel@yahoo.com'),"
                        + "('Vikram Sinha', 103, '9765432108', 'vikram.sinha@hotmail.com'),"
                        + "('Hem Gabhawala', 104, '9898123456', 'hem.104@gmail.com'),"
                        + "('Hir Ray', 201, '9787654321', 'hir.201@yahoo.com'),"
                        + "('Thira Patel', 202, '9678563412', 'thira.202@outlook.com'),"
                        + "('Henisha Mistry', 203, '9856473829', 'henisha.mistry@rediffmail.com'),"
                        + "('Vishv Patel', 204, '9765342187', 'vishv.204@hotmail.com'),"
                        + "('Chirayu Mistry', 301, '9896321475', 'chirayu.301@gmail.com'),"
                        + "('Meet Suthar', 302, '9872316548', 'meet.302@yahoo.com'),"
                        + "('Hardi Patel', 303, '9768451290', 'hardi.303@outlook.com'),"
                        + "('Hardi Parikh', 304, '9687123549', 'hardi.parikh@gmail.com'),"
                        + "('Shubham Parikh', 401, '9865412378', 'shubham.401@yahoo.com'),"
                        + "('Diti Amin', 402, '9798564123', 'diti.402@rediffmail.com'),"
                        + "('Shreya Jadhav', 403, '9876598231', 'shreya.403@hotmail.com'),"
                        + "('Tithi Patel', 404, '9654781236', 'tithi.404@gmail.com'),"
                        + "('Vivek Sir', 501, '9784123654', 'vivek.sir501@outlook.com'),"
                        + "('Avani Mam', 502, '9865324781', 'avani.mam502@yahoo.com'),"
                        + "('Maharshi Bhrambhatt', 503, '9678521436', 'maharshi.503@gmail.com'),"
                        + "('Preet Amin', 504, '9874563219', 'preet.504@hotmail.com');"
                );
            }

            //helper table
            rs = stmt.executeQuery("SELECT COUNT(*) FROM helper");
            rs.next();
            int helperCount = rs.getInt(1);

            if (helperCount == 0) {
                stmt.executeUpdate("INSERT INTO helper (name, role, aadharNumber, contactInformation, salary) VALUES "
                        + "('Ramesh Kumar', 'Security Guard', 123456789012, '9876543210', 15000.00),"
                        + "('Suresh Yadav', 'Cleaner', 123456789013, '9865432109', 12000.00),"
                        + "('Manju Devi', 'Maid', 123456789014, '9765432108', 10000.00),"
                        + "('Anita Sharma', 'Cook', 123456789015, '9856321470', 11000.00),"
                        + "('Sunil Verma', 'Electrician', 123456789016, '9845213698', 16000.00),"
                        + "('Geeta Rani', 'Gardener', 123456789017, '9834567890', 9500.00),"
                        + "('Vikram Singh', 'Plumber', 123456789018, '9823456781', 14000.00)");
            }

            //Maintenance Table
            rs = stmt.executeQuery("SELECT COUNT(*) FROM maintenance");
            rs.next();
            int maintenanceCount = rs.getInt(1);

            if (maintenanceCount == 0) {
                stmt.executeUpdate("INSERT INTO maintenance (name, apartmentNumber, contactInformation, amountPaid, modeOfPayment, paymentDate) VALUES "
                        + "('Ravi Sharma', 101, '9876543210', 5000.00, 'Online', '2025-01-10'),"
                        + "('Neha Patel', 102, '9865432109', 4500.00, 'Cheque', '2025-01-12'),"
                        + "('Vikram Sinha', 103, '9765432108', 4000.00, 'Cash', '2025-01-15');");
            }

            //complaint table
            rs = stmt.executeQuery("SELECT COUNT(*) FROM complaint");
            rs.next();
            int complaintCount = rs.getInt(1);

            if (complaintCount == 0) {
                stmt.executeUpdate("INSERT INTO complaint (apartmentNumber, description, dateFiled) VALUES "
                        + "(101, 'Water leakage in bathroom.', '2025-01-20'),"
                        + "(102, 'Noise disturbance from neighbors.', '2025-01-22'),"
                        + "(103, 'Lift not working.', '2025-01-25');");

            }

            // Committee table
            rs = stmt.executeQuery("SELECT COUNT(*) FROM Committee");
            rs.next();
            int committeeCount = rs.getInt(1);

            if (committeeCount == 0) {
                stmt.executeUpdate("INSERT INTO Committee (name, description, head, apartmentNumber) VALUES "
                        + "('Maintenance', 'Handles maintenance and repair work.', 'Ravi Sharma', 101),"
                        + "('Event', 'Organizes cultural and social events.', 'Neha Patel', 102),"
                        + "('Security', 'Ensures society safety and security.', 'Vikram Sinha', 103);");
            }

            //Notice table
            rs = stmt.executeQuery("SELECT COUNT(*) FROM notice");
            rs.next();
            int noticeCount = rs.getInt(1);

            if (noticeCount == 0) {
                stmt.executeUpdate("INSERT INTO notice (date, title, content, issuedBy) VALUES "
                        + "('2025-01-15', 'Water Supply', 'Water supply will be disrupted from 9 AM to 5 PM.', '1'),"
                        + "('2025-01-20', 'Parking Rules', 'No double parking allowed.', '3'),"
                        + "('2025-01-25', 'Maintenance Due', 'Submit maintenance payments by 31st Jan.', '3'),"
                        + "('2025-02-01', 'Fire Drill', 'Mandatory fire drill scheduled on 3rd Feb at 10 AM.', '2'),"
                        + "('2025-02-05', 'Lift Maintenance', 'Lifts in Block B will be under maintenance on 6th Feb.', '1'),"
                        + "('2025-02-10', 'Society Meeting', 'All residents are invited to the society meeting on 12th Feb.', '1'),"
                        + "('2025-02-15', 'Playground Cleaning', 'Playground will be closed for cleaning from 8 AM to 12 PM.', '2')");
            }

            //vehicle table
            rs = stmt.executeQuery("SELECT COUNT(*) FROM vehicle");
            rs.next();
            int vehicleCount = rs.getInt(1);

            if (vehicleCount == 0) {
                stmt.executeUpdate("INSERT INTO vehicle (number, type, owner, apartmentNumber) VALUES "
                        + "('GJ01AB1234', 'Car', 'Ravi Sharma', 101), "
                        + "('GJ01CD5678', 'Bike', 'Neha Patel', 102), "
                        + "('GJ01EF9012', 'Car', 'Vikram Sinha', 103), "
                        + "('GJ01GH3456', 'Scooter', 'Hem Gabbawala', 104), "
                        + "('GJ01IJ7890', 'Car', 'Hir Ray', 201), "
                        + "('GJ02KL1234', 'Bike', 'Thira Patel', 202), "
                        + "('GJ02MN5678', 'Car', 'Rudra Soni', 203), "
                        + "('GJ02OP9012', 'Scooter', 'Vishv Patel', 204), "
                        + "('GJ02QR3456', 'Car', 'Chirayu Vaghela', 301), "
                        + "('GJ02ST7890', 'Bike', 'Meet Suthar', 302), "
                        + "('GJ03UV1234', 'Car', 'Hardi Patel', 303), "
                        + "('GJ03WX5678', 'Bike', 'Hardi Parikh', 304), "
                        + "('GJ03YZ9012', 'Car', 'Shubham Parikh', 401), "
                        + "('GJ03AB3456', 'Scooter', 'Diti Amin', 402), "
                        + "('GJ03CD7890', 'Car', 'Shreya Jadhav', 403), "
                        + "('GJ04EF1234', 'Bike', 'Tithi Patel', 404), "
                        + "('GJ04GH5678', 'Car', 'Vivek Sir', 501), "
                        + "('GJ04IJ9012', 'Scooter', 'Avani Mam', 502), "
                        + "('GJ04KL3456', 'Car', 'Maharishi', 503), "
                        + "('GJ04MN7890', 'Car', 'Preet Amin', 504), "
                        + "('GJ05OP1234', 'Bike', 'Ravi Sharma', 101), "
                        + "('GJ05QR5678', 'Scooter', 'Neha Patel', 102), "
                        + "('GJ05ST9012', 'Car', 'Vikram Sinha', 103), "
                        + "('GJ05UV3456', 'Bike', 'Hem Gabbawala', 104), "
                        + "('GJ05WX7890', 'Car', 'Hir Ray', 201);");
            }

            //Apartment table
            rs = stmt.executeQuery("SELECT COUNT(*) FROM apartment");
            rs.next();
            int apartmentCount = rs.getInt(1);

            if (apartmentCount == 0) {
                stmt.executeUpdate("INSERT INTO apartment (apartmentNumber, type, name) VALUES "
                        + "(101, 'Owner', 'Ravi Sharma'),"
                        + "(102, 'Owner', 'Neha Patel'),"
                        + "(103, 'Tenant', 'Vikram Sinha'),"
                        + "(104, 'Tenant', 'Hem'),"
                        + "(201, 'Owner', 'Hir'),"
                        + "(202, 'Tenant', 'Thira'),"
                        + "(203, 'Owner', 'Henisha Mistry'),"
                        + "(204, 'Owner', 'Vishv'),"
                        + "(301, 'Tenant', 'Chirayu'),"
                        + "(302, 'Owner', 'Meet'),"
                        + "(303, 'Owner', 'Hardi'),"
                        + "(304, 'Owner', 'Hardi Parikh'),"
                        + "(401, 'Tenant', 'Shubham'),"
                        + "(402, 'Tenant', 'Diti'),"
                        + "(403, 'Owner', 'Shreya'),"
                        + "(404, 'Owner', 'Tithi'),"
                        + "(501, 'Tenant', 'Vivek Sir'),"
                        + "(502, 'Tenant', 'Avani Mam'),"
                        + "(503, 'Owner', 'Maharshi'),"
                        + "(504, 'Owner', 'Preet');");
            }

            System.out.println("Hardcoded sample users inserted into 'users' table.");
            rs.close();
            stmt.close();
        }
    }
}
