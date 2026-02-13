package org.juv25d.filter;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Filter that handles URL redirects based on configurable rules.
 *
 * When a request matches a redirect rule:
 * - The pipeline is stopped (no further processing)
 * - A redirect response is returned with the appropriate Location header
 * - Status code is either 301 (Moved Permanently) or 302 (Found/Temporary)
 *
 * Rules are evaluated in order - the first matching rule wins.
 *
 * Example usage:
 * <pre>
 * List<RedirectRule> rules = List.of(
 *     new RedirectRule("/old-page", "/new-page", 301),
 *     new RedirectRule("/temp", "https://example.com", 302),
 *     new RedirectRule("/docs/*", "/documentation/", 301)
 * );
 * pipeline.addFilter(new RedirectFilter(rules));
 * </pre>
 */
public class RedirectFilter implements Filter {
    private final List<RedirectRule> rules;
    private final Logger logger;

    /**
     * Creates a redirect filter with the given rules.
     *
     * @param rules List of redirect rules (evaluated in order)
     */
    public RedirectFilter(List<RedirectRule> rules) {
        this.rules = rules;
        this.logger = Logger.getLogger(RedirectFilter.class.getName());
    }

    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) throws IOException {
        String requestPath = request.path();

        // Check each rule in order - first match wins
        for (RedirectRule rule : rules) {
            if (rule.matches(requestPath)) {
                logger.info("Redirecting: " + requestPath + " -> " + rule.getTargetUrl()
                    + " (" + rule.getStatusCode() + ")");

                performRedirect(response, rule);

                // Stop pipeline - don't call chain.doFilter()
                return;
            }
        }

        // No matching rule - continue pipeline
        chain.doFilter(request, response);
    }

    /**
     * Sets up the redirect response with appropriate headers.
     *
     * @param response The HTTP response to modify
     * @param rule The redirect rule to apply
     */
    private void performRedirect(HttpResponse response, RedirectRule rule) {
        int statusCode = rule.getStatusCode();
        String statusText = statusCode == 301 ? "Moved Permanently" : "Found";

        response.setStatusCode(statusCode);
        response.setStatusText(statusText);
        response.setHeader("Location", rule.getTargetUrl());
        response.setHeader("Content-Length", "0");

        // Empty body for redirects
        response.setBody(new byte[0]);
    }
}
