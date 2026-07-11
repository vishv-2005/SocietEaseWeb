package filters;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * RateLimitFilter — Simple per-IP sliding-window rate limiter.
 *
 * Defaults: max 120 requests per 60-second window per client IP.
 * Configurable via web.xml init-params "maxRequests" and "windowSeconds".
 *
 * Returns HTTP 429 (Too Many Requests) when the limit is exceeded.
 * Runs after BotBlockFilter and before SecurityHeadersFilter/AuthFilter
 * so that blocked bots never consume rate-limit slots.
 *
 * NOTE: This is an in-memory, single-instance limiter. If you scale to
 * multiple Tomcat instances behind a load balancer, replace this with
 * Redis-based rate limiting or an API gateway.
 */
@WebFilter(filterName = "RateLimitFilter", urlPatterns = "/*")
public class RateLimitFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(RateLimitFilter.class.getName());

    private int maxRequests = 120;       // requests per window
    private long windowMillis = 60_000L; // 60 seconds

    // IP → queue of request timestamps (epoch millis)
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Long>> requestLog
            = new ConcurrentHashMap<>();

    // Track last cleanup to avoid running it on every request
    private volatile long lastCleanup = System.currentTimeMillis();
    private static final long CLEANUP_INTERVAL = 120_000L; // 2 minutes

    @Override
    public void init(FilterConfig config) throws ServletException {
        String maxReqParam = config.getInitParameter("maxRequests");
        String windowParam = config.getInitParameter("windowSeconds");

        if (maxReqParam != null) {
            try { maxRequests = Integer.parseInt(maxReqParam); }
            catch (NumberFormatException ignored) { /* keep default */ }
        }
        if (windowParam != null) {
            try { windowMillis = Long.parseLong(windowParam) * 1000L; }
            catch (NumberFormatException ignored) { /* keep default */ }
        }

        LOGGER.info("RateLimitFilter initialized: " + maxRequests
                     + " requests per " + (windowMillis / 1000) + "s window.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                          FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip rate limiting for static resources (CSS, JS, images, favicon)
        String path = httpRequest.getServletPath();
        if (path != null && (path.startsWith("/css/") || path.startsWith("/js/")
                || path.startsWith("/images/") || path.equals("/favicon.ico"))) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(httpRequest);
        long now = System.currentTimeMillis();

        // Periodic cleanup of stale entries
        if (now - lastCleanup > CLEANUP_INTERVAL) {
            lastCleanup = now;
            cleanupStaleEntries(now);
        }

        // Get or create the request queue for this IP
        ConcurrentLinkedQueue<Long> timestamps = requestLog.computeIfAbsent(
                clientIp, k -> new ConcurrentLinkedQueue<>());

        // Evict timestamps outside the current window
        long windowStart = now - windowMillis;
        while (!timestamps.isEmpty() && timestamps.peek() < windowStart) {
            timestamps.poll();
        }

        // Check limit
        if (timestamps.size() >= maxRequests) {
            LOGGER.warning("Rate limit exceeded for IP: " + clientIp
                           + " (" + timestamps.size() + " requests in window)");
            httpResponse.setStatus(429); // HTTP 429 Too Many Requests
            httpResponse.setHeader("Retry-After", String.valueOf(windowMillis / 1000));
            httpResponse.setContentType("text/plain;charset=UTF-8");
            httpResponse.getWriter().write("Too many requests. Please try again later.");
            return;
        }

        // Record this request
        timestamps.add(now);

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        requestLog.clear();
        LOGGER.info("RateLimitFilter destroyed.");
    }

    /**
     * Extract client IP, respecting X-Forwarded-For behind Railway's reverse proxy.
     *
     * Railway (like most reverse proxies) APPENDS the real client IP to the
     * right side of X-Forwarded-For. A malicious client can prepend fake IPs
     * on the left to try to bypass rate limiting. So we trust only the LAST
     * (rightmost) IP in the chain — that's the one Railway's edge wrote.
     *
     * Format: X-Forwarded-For: <client-supplied>, ..., <railway-added>
     */
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            String[] ips = xff.split(",");
            // Take the LAST IP — the one added by Railway's trusted proxy
            return ips[ips.length - 1].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Remove IPs whose last request is older than the current window.
     * Prevents unbounded ConcurrentHashMap growth from one-off visitors.
     */
    private void cleanupStaleEntries(long now) {
        long windowStart = now - windowMillis;
        Iterator<Map.Entry<String, ConcurrentLinkedQueue<Long>>> it =
                requestLog.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ConcurrentLinkedQueue<Long>> entry = it.next();
            Queue<Long> q = entry.getValue();
            // Evict old timestamps
            while (!q.isEmpty() && q.peek() < windowStart) {
                q.poll();
            }
            // If nothing left, remove the IP entirely
            if (q.isEmpty()) {
                it.remove();
            }
        }
    }
}
