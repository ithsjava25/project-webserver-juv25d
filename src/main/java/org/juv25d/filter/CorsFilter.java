package org.juv25d.filter;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.util.ConfigLoader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CorsFilter implements Filter {

    private final Set <String> allowedOrigins;
    private final String allowedMethods;

    public CorsFilter() {
        ConfigLoader config = ConfigLoader.getInstance();
        this.allowedOrigins = new HashSet<>(config.getAllowedOrigins());
        this.allowedMethods = String.join(",", config.getAllowedMethods());
    }

    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        String origin = header(req.headers(), "Origin");

        // No Origin header || no browser cross-origin req, No CORS headers needed
        if (origin == null || origin.isBlank()) {
            chain.doFilter(req, res);
            return;
        }

        // Origin exists but are not allowed, return no CORS headers
        if (!allowedOrigins.contains(origin)) {
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
            res.setHeader("Access-Control-Allow-Methods", allowedMethods);

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

    @org.jspecify.annotations.Nullable private String header(Map<String, String> headers, @org.jspecify.annotations.Nullable String key) {
        if (key == null) return null;
        for (var entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
