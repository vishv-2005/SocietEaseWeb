package util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AES-256-GCM encryption utility for protecting Personally Identifiable Information (PII).
 * 
 * Usage:
 *   String encrypted = EncryptionUtil.encrypt("Sensitive Data");
 *   String decrypted = EncryptionUtil.decrypt(encrypted);
 * 
 * The encryption key is read from the environment variable SOCIETEASE_ENCRYPT_KEY.
 * If not set, a default key is used (development only — NEVER in production).
 */
public class EncryptionUtil {

    private static final Logger LOGGER = Logger.getLogger(EncryptionUtil.class.getName());
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int IV_LENGTH = 12; // bytes (recommended for GCM)
    private static final int KEY_LENGTH = 256; // bits

    private static SecretKey secretKey;

    static {
        try {
            String envKey = System.getenv("SOCIETEASE_ENCRYPT_KEY");
            if (envKey != null && !envKey.isEmpty()) {
                // Decode Base64-encoded key from environment variable
                byte[] keyBytes = Base64.getDecoder().decode(envKey);
                secretKey = new SecretKeySpec(keyBytes, "AES");
                LOGGER.info("Encryption key loaded from environment variable.");
            } else {
                // Development fallback — generate a consistent key from a fixed seed
                // WARNING: Replace this with a proper env var in production!
                LOGGER.warning("SOCIETEASE_ENCRYPT_KEY not set! Using development fallback key. NOT SAFE FOR PRODUCTION.");
                byte[] defaultKey = "SocietEase256BitSecretKey!!12345".getBytes(StandardCharsets.UTF_8);
                secretKey = new SecretKeySpec(defaultKey, 0, 32, "AES");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize encryption key", e);
            throw new RuntimeException("Encryption initialization failed", e);
        }
    }

    /**
     * Encrypts plaintext using AES-256-GCM.
     * Returns Base64-encoded string containing IV + ciphertext.
     */
    public static String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Prepend IV to ciphertext
            byte[] combined = new byte[IV_LENGTH + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(cipherText, 0, combined, IV_LENGTH, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypts a Base64-encoded AES-256-GCM ciphertext.
     */
    public static String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            // Extract IV
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);

            // Extract ciphertext
            byte[] cipherText = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Generates a new random AES-256 key and prints it as Base64.
     * Use this to generate the value for the SOCIETEASE_ENCRYPT_KEY env var.
     */
    public static String generateNewKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_LENGTH);
            SecretKey key = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Key generation failed", e);
        }
    }
}
