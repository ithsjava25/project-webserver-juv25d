package org.juv25d.filter;

import org.juv25d.auth.Session;
import org.juv25d.auth.SessionStore;
import org.juv25d.filter.annotation.Global;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.util.CookieUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Global filter that enforces server-side session authentication.
 *
 * Public (unauthenticated) paths: /login, /logout, /health, /metric and static assets (/css, /js, /images, /favicon.ico, /, /index.html, /readme.html).
 * For protected paths:
 *   - If no valid session cookie (SID) is present: redirect GET to /login, return 401 for non-GET.
 *   - If valid session exists: continue the chain and expose X-Authenticated and X-User headers.
 */
@Global(order = 6)
public class SessionAuthFilter implements Filter {

    private static final String COOKIE_NAME = "SID";

    @Override
    public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) throws IOException {
        String path = req.path();

        if (isPublicPath(path)) {
            chain.doFilter(req, res);
            return;
        }

        String sid = CookieUtils.readCookie(req, COOKIE_NAME);
        Session session = sid != null ? SessionStore.getInstance().get(sid) : null;
        if (session == null) {
            // Unauthenticated
            if ("GET".equalsIgnoreCase(req.method())) {
                res.setStatusCode(302);
                res.setStatusText("Found");
                res.setHeader("Location", "/login");
                res.setHeader("Cache-Control", "no-store");
                res.setHeader("Content-Length", "0");
                res.setBody(new byte[0]);
            } else {
                res.setStatusCode(401);
                res.setStatusText("Unauthorized");
                res.setHeader("Cache-Control", "no-store");
                byte[] body = "Unauthorized".getBytes(StandardCharsets.UTF_8);
                res.setHeader("Content-Type", "text/plain; charset=UTF-8");
                res.setHeader("Content-Length", String.valueOf(body.length));
                res.setBody(body);
            }
            return;
        }

        // Authenticated: proceed
        chain.doFilter(req, res);
    }

    private boolean isPublicPath(String path) {
        if (path == null || path.isEmpty()) return true;
        if ("/login".equals(path) || "/logout".equals(path)) return true;
        if ("/session/status".equals(path)) return true; // tillåt status-endpoint utan auth
        if ("/health".equals(path) || "/metric".equals(path)) return true;
        if ("/".equals(path) || "/index.html".equals(path) || "/readme.html".equals(path)) return true;
        return path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/") || "/favicon.ico".equals(path) || path.startsWith("/static/");
    }

}
