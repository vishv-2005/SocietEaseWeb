package filters;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Security headers filter.
 * Sets HTTP security headers on every response to prevent common attacks.
 */
@WebFilter(filterName = "SecurityHeadersFilter", urlPatterns = {"/*"})
public class SecurityHeadersFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(SecurityHeadersFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("SecurityHeadersFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Prevent MIME type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Prevent clickjacking
        response.setHeader("X-Frame-Options", "DENY");

        // Enable browser XSS filter
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Referrer policy — don't leak URL on cross-origin requests
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Permissions policy — disable unnecessary browser features
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        // Content Security Policy — only allow resources from same origin + Razorpay + Bootstrap CDN
        response.setHeader("Content-Security-Policy",
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' https://checkout.razorpay.com https://cdn.jsdelivr.net; " +
            "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://fonts.googleapis.com; " +
            "font-src 'self' https://fonts.gstatic.com; " +
            "frame-src https://api.razorpay.com; " +
            "connect-src 'self' https://lumberjack.razorpay.com; " +
            "img-src 'self' data:;"
        );

        // Cache control for authenticated pages
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        LOGGER.info("SecurityHeadersFilter destroyed.");
    }
}
