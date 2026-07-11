package filters;

import java.io.IOException;
import java.util.regex.Pattern;

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
 * BotBlockFilter
 *
 * Rejects obvious vulnerability-scanner / bot requests before they reach
 * AuthFilter, CsrfFilter, or any servlet logic. This app is Java/Tomcat,
 * so paths probing PHP, WordPress, Next.js, exposed .git folders, etc.
 * can never be legitimate here — we return 403 immediately instead of
 * spending memory/CPU on session lookups, redirects, and logging.
 *
 * IMPORTANT: This filter must run FIRST in the chain, before AuthFilter
 * and CsrfFilter. See web.xml ordering notes in the prompt/instructions.
 */
@WebFilter(filterName = "BotBlockFilter", urlPatterns = "/*")
public class BotBlockFilter implements Filter {

    // One combined pattern is cheaper to evaluate per-request than a list of
    // String.contains() checks. Add new signatures here as you spot them in logs.
    private static final Pattern BLOCKED_PATTERN = Pattern.compile(
            ".*(" +
            "\\.git/|" +                    // exposed git metadata
            "\\.env$|" +                    // exposed environment files
            "wp-json|wp-includes|wlwmanifest|wp-admin|" + // WordPress probes
            "vendor/phpunit|eval-stdin\\.php|" +           // PHPUnit RCE probe
            "laravel/|yii/|zend/|" +        // PHP framework probes
            "swagger\\.json|" +             // exposed API docs probe
            "_next|_rsc|__rsc|__nextjs_action|_middleware|" + // Next.js probes
            "\\.php$|\\.asp$|\\.aspx$|" +   // non-Java extensions we never serve
            "phpmyadmin|adminer" +
            ").*",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no setup needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                          FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();

        if (uri != null && BLOCKED_PATTERN.matcher(uri).matches()) {
            // Return 403 immediately — no session creation, no auth check,
            // no CSRF check, no forward to login.jsp. Cheapest possible response.
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return; // stop the chain here — do NOT call chain.doFilter()
        }

        // Not a known bad pattern — continue to AuthFilter/CsrfFilter as normal
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // no cleanup needed
    }
}
