package org.juv25d.filter;

import java.util.regex.Pattern;

/**
 * Represents a URL redirect rule.
 *
 * A redirect rule consists of:
 * - sourcePath: The path to match (supports exact match or wildcards with *)
 * - targetUrl: The URL to redirect to
 * - statusCode: HTTP status code (301 for permanent, 302 for temporary)
 *
 * Examples:
 * - new RedirectRule("/old-page", "/new-page", 301)
 * - new RedirectRule("/temp", "https://example.com", 302)
 * - new RedirectRule("/docs/*", "/documentation/", 301)
 */
public class RedirectRule {
    private final String sourcePath;
    private final String targetUrl;
    private final int statusCode;

    /**
     * Creates a redirect rule with exact path matching.
     *
     * @param sourcePath The path to match (e.g., "/old-page" or "/docs/*" for wildcard)
     * @param targetUrl The URL to redirect to
     * @param statusCode HTTP status code (301 or 302)
     */
    public RedirectRule(String sourcePath, String targetUrl, int statusCode) {
        validateStatusCode(statusCode);
        this.sourcePath = sourcePath;
        this.targetUrl = targetUrl;
        this.statusCode = statusCode;
    }

    /**
     * Checks if the given request path matches this rule.
     *
     * Supports:
     * - Exact matching: "/old-page" matches exactly "/old-page"
     * - Wildcard matching: "/docs/*" matches "/docs/api", "/docs/guide", etc.
     *
     * @param requestPath The request path to check
     * @return true if the path matches this rule
     */
    public boolean matches(String requestPath) {
        if (requestPath == null) {
            return false;
        }

        // Check for wildcard matching
        if (sourcePath.contains("*")) {
            // Split on wildcard, escape each literal segment, then rejoin with ".*"
            String[] parts = sourcePath.split("\\*", -1);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                sb.append(Pattern.quote(parts[i]));
                if (i < parts.length - 1) {
                    sb.append(".*");
                }
            }
            return requestPath.matches(sb.toString());
        }

        // Exact match
        return requestPath.equals(sourcePath);
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public int getStatusCode() {
        return statusCode;
    }

    private void validateStatusCode(int statusCode) {
        if (statusCode != 301 && statusCode != 302) {
            throw new IllegalArgumentException(
                "Status code must be 301 (Moved Permanently) or 302 (Found). Got: " + statusCode
            );
        }
    }

    @Override
    public String toString() {
        return "RedirectRule{" +
            "sourcePath='" + sourcePath + '\'' +
            ", targetUrl='" + targetUrl + '\'' +
            ", statusCode=" + statusCode +
            '}';
    }
}
