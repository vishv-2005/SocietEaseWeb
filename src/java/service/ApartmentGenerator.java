package service;

import storage.DBConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates towers and apartments for a newly registered society.
 * 
 * Towers are labeled with letters: A, B, C, ...
 * Apartments use the format: {TowerLetter}-{Floor}{Unit}
 * Example: For Tower A, Floor 3, Unit 2 → "A-302"
 * 
 * All generated apartments start with status = VACANT.
 */
public class ApartmentGenerator {

    private static final Logger LOGGER = Logger.getLogger(ApartmentGenerator.class.getName());

    /**
     * Generates all towers and apartments for a society.
     * 
     * @param societyId       The society ID
     * @param totalTowers     Number of towers (1-26, mapped to A-Z)
     * @param floorsPerTower  Floors per tower (1-99)
     * @param unitsPerFloor   Units per floor (1-99)
     * @return Total number of apartments generated
     */
    public static int generateApartments(int societyId, int totalTowers, int floorsPerTower, int unitsPerFloor)
            throws SQLException {

        int totalApartments = 0;

        try (Connection conn = DBConnector.getConnection()) {
            conn.setAutoCommit(false); // Transaction for atomicity

            try {
                for (int t = 0; t < totalTowers; t++) {
                    String towerName = "Tower " + (char) ('A' + t);
                    String towerLetter = String.valueOf((char) ('A' + t));

                    // Insert tower
                    int towerId;
                    String towerSql = "INSERT INTO tower (society_id, tower_name, total_floors, units_per_floor) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(towerSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        stmt.setInt(1, societyId);
                        stmt.setString(2, towerName);
                        stmt.setInt(3, floorsPerTower);
                        stmt.setInt(4, unitsPerFloor);
                        stmt.executeUpdate();

                        try (ResultSet keys = stmt.getGeneratedKeys()) {
                            keys.next();
                            towerId = keys.getInt(1);
                        }
                    }

                    // Generate apartments for this tower
                    String aptSql = "INSERT INTO apartment (society_id, tower_id, floor_number, unit_number, apartment_label, status) " +
                                    "VALUES (?, ?, ?, ?, ?, 'VACANT')";
                    try (PreparedStatement stmt = conn.prepareStatement(aptSql)) {
                        for (int floor = 1; floor <= floorsPerTower; floor++) {
                            for (int unit = 1; unit <= unitsPerFloor; unit++) {
                                // Label format: A-301 (Tower A, Floor 3, Unit 01)
                                String label = towerLetter + "-" + floor + String.format("%02d", unit);

                                stmt.setInt(1, societyId);
                                stmt.setInt(2, towerId);
                                stmt.setInt(3, floor);
                                stmt.setInt(4, unit);
                                stmt.setString(5, label);
                                stmt.addBatch();
                                totalApartments++;
                            }
                        }
                        stmt.executeBatch();
                    }

                    LOGGER.info("Generated " + towerName + " with " + (floorsPerTower * unitsPerFloor) + " apartments.");
                }

                conn.commit();
                LOGGER.info("Total apartments generated for society " + societyId + ": " + totalApartments);

            } catch (SQLException e) {
                conn.rollback();
                LOGGER.log(Level.SEVERE, "Failed to generate apartments, rolling back.", e);
                throw e;
            }
        }

        return totalApartments;
    }

    /**
     * Generates individual units for non-apartment societies (villas, bungalows, duplexes, row houses, etc.).
     *
     * @param societyId      The society ID
     * @param unitLabel      What units are called (Villa, House, Plot, etc.)
     * @param hasBlocks      Whether the society has blocks/wings/sectors
     * @param blockLabel     What blocks are called (Block, Wing, Sector, Phase, etc.)
     * @param totalBlocks    Number of blocks (1-26, used only if hasBlocks)
     * @param unitsPerBlock  Units per block (used if hasBlocks), or total units (if !hasBlocks)
     * @param numberingStyle BLOCK_UNIT, FULL_LABEL, PREFIX, or PLAIN
     * @return Total number of units generated
     */
    public static int generateIndividualUnits(int societyId, String unitLabel, boolean hasBlocks,
            String blockLabel, int totalBlocks, int unitsPerBlock, String numberingStyle)
            throws SQLException {

        int totalUnits = 0;

        try (Connection conn = DBConnector.getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (hasBlocks) {
                    // Create each block as a "tower"
                    for (int b = 0; b < totalBlocks; b++) {
                        char blockLetter = (char) ('A' + b);
                        String towerName = blockLabel + " " + blockLetter;

                        // Insert tower (block)
                        int towerId;
                        String towerSql = "INSERT INTO tower (society_id, tower_name, total_floors, units_per_floor) VALUES (?, ?, 1, ?)";
                        try (PreparedStatement stmt = conn.prepareStatement(towerSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                            stmt.setInt(1, societyId);
                            stmt.setString(2, towerName);
                            stmt.setInt(3, unitsPerBlock);
                            stmt.executeUpdate();

                            try (ResultSet keys = stmt.getGeneratedKeys()) {
                                keys.next();
                                towerId = keys.getInt(1);
                            }
                        }

                        // Generate units for this block
                        String aptSql = "INSERT INTO apartment (society_id, tower_id, floor_number, unit_number, apartment_label, status) " +
                                        "VALUES (?, ?, 1, ?, ?, 'VACANT')";
                        try (PreparedStatement stmt = conn.prepareStatement(aptSql)) {
                            for (int u = 1; u <= unitsPerBlock; u++) {
                                String label;
                                switch (numberingStyle) {
                                    case "FULL_LABEL":
                                        // Block-A-Villa-1
                                        String abbr = unitLabel.replaceAll("\\s+", "");
                                        if (abbr.length() > 3) abbr = abbr.substring(0, 3);
                                        label = blockLabel + "-" + blockLetter + "-" + abbr + u;
                                        break;
                                    case "PLAIN":
                                        // Sequential across all blocks
                                        label = String.valueOf(b * unitsPerBlock + u);
                                        break;
                                    case "BLOCK_UNIT":
                                    default:
                                        // A-1, B-3
                                        label = blockLetter + "-" + u;
                                        break;
                                }

                                stmt.setInt(1, societyId);
                                stmt.setInt(2, towerId);
                                stmt.setInt(3, u);
                                stmt.setString(4, label);
                                stmt.addBatch();
                                totalUnits++;
                            }
                            stmt.executeBatch();
                        }

                        LOGGER.info("Generated " + towerName + " with " + unitsPerBlock + " " + unitLabel + "(s).");
                    }
                } else {
                    // No blocks — create a single virtual tower
                    int towerId;
                    String towerSql = "INSERT INTO tower (society_id, tower_name, total_floors, units_per_floor) VALUES (?, 'Main', 1, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(towerSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        stmt.setInt(1, societyId);
                        stmt.setInt(2, unitsPerBlock); // unitsPerBlock holds totalUnits in this case
                        stmt.executeUpdate();

                        try (ResultSet keys = stmt.getGeneratedKeys()) {
                            keys.next();
                            towerId = keys.getInt(1);
                        }
                    }

                    // Generate all units
                    String aptSql = "INSERT INTO apartment (society_id, tower_id, floor_number, unit_number, apartment_label, status) " +
                                    "VALUES (?, ?, 1, ?, ?, 'VACANT')";
                    try (PreparedStatement stmt = conn.prepareStatement(aptSql)) {
                        for (int u = 1; u <= unitsPerBlock; u++) {
                            String label;
                            if ("PREFIX".equals(numberingStyle)) {
                                // Villa-1, House-2
                                label = unitLabel.replaceAll("\\s+", "-") + "-" + u;
                            } else {
                                // Plain: 1, 2, 3
                                label = String.valueOf(u);
                            }

                            stmt.setInt(1, societyId);
                            stmt.setInt(2, towerId);
                            stmt.setInt(3, u);
                            stmt.setString(4, label);
                            stmt.addBatch();
                            totalUnits++;
                        }
                        stmt.executeBatch();
                    }

                    LOGGER.info("Generated " + unitsPerBlock + " " + unitLabel + "(s) for society " + societyId + ".");
                }

                conn.commit();
                LOGGER.info("Total units generated for society " + societyId + ": " + totalUnits);

            } catch (SQLException e) {
                conn.rollback();
                LOGGER.log(Level.SEVERE, "Failed to generate units, rolling back.", e);
                throw e;
            }
        }

        return totalUnits;
    }
}
