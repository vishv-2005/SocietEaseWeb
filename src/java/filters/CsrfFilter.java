package filters;

import util.CsrfUtil;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * CSRF protection filter.
 * Validates CSRF tokens on all POST requests to prevent cross-site request forgery.
 * GET requests are exempt (they should be idempotent).
 */
@WebFilter(filterName = "CsrfFilter", urlPatterns = {"/*"})
public class CsrfFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(CsrfFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("CsrfFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Only validate POST requests
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String path = request.getServletPath();

            // Exempt login and registration from CSRF (they create the session)
            if ("/LoginServlet".equals(path) || "/RegisterSocietyServlet".equals(path)) {
                chain.doFilter(request, response);
                return;
            }

            // Exempt API endpoints that use other authentication (e.g., Razorpay webhooks)
            if (path.startsWith("/api/webhook")) {
                chain.doFilter(request, response);
                return;
            }

            // Validate CSRF token for all other POST requests
            if (!CsrfUtil.validateToken(request)) {
                LOGGER.warning("CSRF token validation failed for: " + path);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, 
                    "Security validation failed. Please refresh the page and try again.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        LOGGER.info("CsrfFilter destroyed.");
    }
}
