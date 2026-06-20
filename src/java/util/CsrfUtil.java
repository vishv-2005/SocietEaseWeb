package util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * CSRF (Cross-Site Request Forgery) protection utility.
 * 
 * Usage in JSP:
 *   <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}">
 * 
 * Usage in Servlet (validation):
 *   if (!CsrfUtil.validateToken(request)) {
 *       response.sendError(403, "Invalid CSRF token");
 *       return;
 *   }
 */
public class CsrfUtil {

    private static final Logger LOGGER = Logger.getLogger(CsrfUtil.class.getName());
    public static final String CSRF_TOKEN_ATTR = "csrf_token";
    public static final String CSRF_TOKEN_PARAM = "csrf_token";
    private static final int TOKEN_LENGTH = 32;

    /**
     * Generates a new CSRF token and stores it in the session.
     * If a token already exists in the session, returns the existing one.
     */
    public static String getOrCreateToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(CSRF_TOKEN_ATTR);
        if (token == null || token.isEmpty()) {
            token = generateToken();
            session.setAttribute(CSRF_TOKEN_ATTR, token);
        }
        return token;
    }

    /**
     * Regenerates the CSRF token (call after successful form submission).
     */
    public static String regenerateToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = generateToken();
        session.setAttribute(CSRF_TOKEN_ATTR, token);
        return token;
    }

    /**
     * Validates the CSRF token from the request parameter against the session token.
     */
    public static boolean validateToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            LOGGER.warning("CSRF validation failed: no session");
            return false;
        }

        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTR);
        String requestToken = request.getParameter(CSRF_TOKEN_PARAM);

        if (sessionToken == null || requestToken == null) {
            LOGGER.warning("CSRF validation failed: missing token");
            return false;
        }

        // Constant-time comparison to prevent timing attacks
        boolean valid = constantTimeEquals(sessionToken, requestToken);
        if (!valid) {
            LOGGER.warning("CSRF validation failed: token mismatch");
        }
        return valid;
    }

    private static String generateToken() {
        byte[] bytes = new byte[TOKEN_LENGTH];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
