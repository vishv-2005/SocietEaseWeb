package util;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads application configuration from config.properties on the classpath.
 * Falls back to System.getProperty() and System.getenv() if not found.
 * 
 * Priority order:
 *   1. config.properties (on classpath)
 *   2. System.getProperty() (-D flags)
 *   3. System.getenv() (environment variables)
 */
public class AppConfig {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final Properties props = new Properties();

    static {
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                props.load(is);
                LOGGER.info("config.properties loaded successfully (" + props.size() + " properties).");
            } else {
                LOGGER.warning("config.properties not found on classpath. Using system properties/env only.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load config.properties", e);
        }
    }

    /**
     * Get a config value. Checks config.properties first, then System property, then env var.
     */
    public static String get(String key) {
        return get(key, "");
    }

    /**
     * Get a config value with a default fallback.
     */
    public static String get(String key, String defaultValue) {
        // 1. Check config.properties
        String value = props.getProperty(key);
        if (value != null && !value.trim().isEmpty()) return value.trim();

        // 2. Check System property (-D flag)
        value = System.getProperty(key);
        if (value != null && !value.trim().isEmpty()) return value.trim();

        // 3. Check environment variable
        value = System.getenv(key);
        if (value != null && !value.trim().isEmpty()) return value.trim();

        return defaultValue;
    }
}
