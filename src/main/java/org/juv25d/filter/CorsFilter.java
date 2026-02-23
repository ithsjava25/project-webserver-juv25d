package org.juv25d.filter;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class CorsFilter implements Filter {

    // Whitelist, allow known origins:
    private static final Set<String> ALLOWED_ORIGINS = Set.of(
        "http://localhost:3000"
    );

    // Supported methods
    private static final String ALLOWED_METHODS = "GET, POST, PUT, PATCH, DELETE, OPTIONS";

    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        String origin = header(req.headers(), "Origin");

        // No Origin header || no browser cross-origin req, No CORS headers needed
        if (origin == null || origin.isBlank()) {
            chain.doFilter(req, res);
            return;
        }

        // Origin exists but are not allowed, return no CORS headers
        if (!ALLOWED_ORIGINS.contains(origin)) {
            chain.doFilter(req, res);
            return;
        }

        // Allowed origin => AC-AO on all res, even GET
        res.setHeader("Access-Control-Allow-Origin", origin);
        String vary = res.getHeader("Vary");
        if (vary == null || vary.isBlank()) {
            res.setHeader("Vary", "Origin");
        } else if (!vary.toLowerCase().contains("origin")) {
            res.setHeader("Vary", vary + ", Origin");
        }

        // Preflight, OPTIONS
        if ("OPTIONS".equalsIgnoreCase(req.method())) {
            res.setHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);

            // If browser requests specific headers, mirror
            String requestedHeaders = header(req.headers(), "Access-Control-Request-Headers");
            if (requestedHeaders != null && !requestedHeaders.isBlank()) {
                res.setHeader("Access-Control-Allow-Headers", requestedHeaders);
            } else {
                res.setHeader("Access-Control-Allow-Headers", "Content-Type");
            }
            res.setHeader("Access-Control-Max-Age", "3600");
            res.setStatusText("No Content");
            res.setStatusCode(204);
            res.setBody(new byte[0]);
            return;
        }
        // Regular request (GET/POST)
        chain.doFilter(req, res);
    }

    private String header(Map<String, String> headers, String key) {
        for (var entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
