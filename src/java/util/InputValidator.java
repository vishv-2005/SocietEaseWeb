package util;

import java.util.regex.Pattern;

/**
 * Centralized input validation and sanitization utility.
 * Prevents injection attacks and handles malformed input gracefully.
 */
public class InputValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[0-9]{10,15}$"
    );

    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[A-Za-z\\s.'-]{1,100}$"
    );

    private static final Pattern PINCODE_PATTERN = Pattern.compile(
        "^[0-9]{5,10}$"
    );

    private static final Pattern VEHICLE_NUMBER_PATTERN = Pattern.compile(
        "^[A-Z0-9\\s-]{4,20}$"
    );

    private static final Pattern AADHAR_PATTERN = Pattern.compile(
        "^[0-9]{12}$"
    );

    /**
     * Safely parses an integer from a string. Returns defaultValue if parsing fails.
     */
    public static int safeParseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Safely parses an integer, throws IllegalArgumentException with a helpful message if invalid.
     */
    public static int requireInt(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }
    }

    /**
     * Validates that a string is not null or empty.
     */
    public static String requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    /**
     * Validates an email address.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates a phone number (10-15 digits).
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates a name (letters, spaces, dots, hyphens, apostrophes).
     */
    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Validates a pincode (5-10 digits).
     */
    public static boolean isValidPincode(String pincode) {
        return pincode != null && PINCODE_PATTERN.matcher(pincode.trim()).matches();
    }

    /**
     * Validates a vehicle number.
     */
    public static boolean isValidVehicleNumber(String number) {
        return number != null && VEHICLE_NUMBER_PATTERN.matcher(number.trim().toUpperCase()).matches();
    }

    /**
     * Validates an Aadhar number (12 digits).
     */
    public static boolean isValidAadhar(String aadhar) {
        return aadhar != null && AADHAR_PATTERN.matcher(aadhar.trim()).matches();
    }

    /**
     * Sanitizes a string by removing HTML tags (basic XSS prevention at input level).
     * Note: This is a defense-in-depth measure. JSTL c:out is the primary XSS defense.
     */
    public static String sanitize(String input) {
        if (input == null) return null;
        return input.replaceAll("<[^>]*>", "")
                     .replaceAll("&", "&amp;")
                     .trim();
    }

    /**
     * Validates that a number is in a positive range.
     */
    public static int requirePositiveRange(String value, String fieldName, int min, int max) {
        int num = requireInt(value, fieldName);
        if (num < min || num > max) {
            throw new IllegalArgumentException(fieldName + " must be between " + min + " and " + max + ".");
        }
        return num;
    }

    /**
     * Validates a password meets minimum requirements.
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        return hasUpper && hasLower && hasDigit;
    }
}
