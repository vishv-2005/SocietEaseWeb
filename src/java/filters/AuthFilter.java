package filters;

import util.CsrfUtil;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Authentication filter that intercepts all requests and enforces login.
 * 
 * Public URLs (no login required):
 *   - / , /index.jsp (landing page)
 *   - /login.jsp, /LoginServlet (login flow)
 *   - /register.jsp, /RegisterSocietyServlet (registration flow)
 *   - /css/*, /images/* (static resources)
 *   - /error/* (error pages)
 * 
 * Role-based access:
 *   - /admin/* → requires role RP or SUPER_ADMIN
 *   - /resident/* → requires role RESIDENT
 *   - /api/admin/* → requires role RP or SUPER_ADMIN
 *   - /api/resident/* → requires role RESIDENT
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthFilter.class.getName());

    // URLs that don't require authentication
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/index.jsp", "/login.jsp", "/register.jsp",
        "/LoginServlet", "/RegisterSocietyServlet",
        "/css/", "/images/", "/error/",
        "/favicon.ico"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("AuthFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getServletPath();
        String contextPath = request.getContextPath();

        // Allow root path
        if ("/".equals(path) || path.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        // Allow public paths
        for (String publicPath : PUBLIC_PATHS) {
            if (path.equals(publicPath) || path.startsWith(publicPath)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            LOGGER.info("Unauthenticated access to: " + path + " — redirecting to login.");
            response.sendRedirect(contextPath + "/login.jsp");
            return;
        }

        String userRole = (String) session.getAttribute("userRole");

        // Ensure CSRF token exists for authenticated users
        CsrfUtil.getOrCreateToken(request);

        // Role-based access control
        if (path.startsWith("/admin/") || path.startsWith("/ManageResidentServlet") ||
            path.startsWith("/ManageHelperServlet") || path.startsWith("/ManageCommitteesServlet") ||
            path.startsWith("/ManageComplaintsServlet") || path.startsWith("/IssueNoticeServlet") ||
            path.startsWith("/MaintenanceRecordsServlet") || path.startsWith("/adminDashboardData") ||
            path.startsWith("/CreateOrderServlet") || path.startsWith("/VerifyPaymentServlet")) {

            if (!"RP".equals(userRole) && !"SUPER_ADMIN".equals(userRole)) {
                LOGGER.warning("Unauthorized admin access attempt by role: " + userRole + " to: " + path);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Admin privileges required.");
                return;
            }
        }

        if (path.startsWith("/resident/")) {
            if (!"RESIDENT".equals(userRole) && !"RP".equals(userRole) && !"SUPER_ADMIN".equals(userRole)) {
                LOGGER.warning("Unauthorized resident access attempt by role: " + userRole + " to: " + path);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        LOGGER.info("AuthFilter destroyed.");
    }
}
