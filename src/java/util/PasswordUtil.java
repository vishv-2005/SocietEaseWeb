package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * Password hashing utility using SHA-256 with per-password salt.
 * 
 * Uses a simple but secure approach that doesn't require external libraries:
 *   - Generates a random 16-byte salt per password
 *   - Hashes with SHA-256 over 10,000 iterations (PBKDF2-style stretching)
 *   - Stores as: Base64(salt) + ":" + Base64(hash)
 * 
 * Usage:
 *   String hashed = PasswordUtil.hashPassword("mypassword");
 *   boolean matches = PasswordUtil.verifyPassword("mypassword", hashed);
 */
public class PasswordUtil {

    private static final Logger LOGGER = Logger.getLogger(PasswordUtil.class.getName());
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 10000;
    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Hashes a password with a random salt.
     * @return "salt:hash" format (both Base64 encoded)
     */
    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);

            byte[] hash = computeHash(password, salt);

            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);

            return saltBase64 + ":" + hashBase64;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }

    /**
     * Verifies a password against a stored hash.
     * @param password The plaintext password to verify
     * @param storedHash The stored "salt:hash" string
     * @return true if the password matches
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            byte[] actualHash = computeHash(password, salt);

            // Constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            LOGGER.warning("Password verification failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Computes an iterated SHA-256 hash with the given salt.
     */
    private static byte[] computeHash(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);

        // Combine password + salt
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] combined = new byte[passwordBytes.length + salt.length];
        System.arraycopy(passwordBytes, 0, combined, 0, passwordBytes.length);
        System.arraycopy(salt, 0, combined, passwordBytes.length, salt.length);

        // Iterate to slow down brute-force attacks
        byte[] hash = digest.digest(combined);
        for (int i = 1; i < ITERATIONS; i++) {
            hash = digest.digest(hash);
        }

        return hash;
    }
}
