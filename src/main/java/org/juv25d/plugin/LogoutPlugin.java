package org.juv25d.plugin;

import org.juv25d.auth.SessionStore;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Logout endpoint for cookie-based server sessions.
 * Invalidates the session identified by the SID cookie and clears the cookie.
 */
public class LogoutPlugin implements Plugin {

    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {
        String sid = readCookie(req, "SID");
        if (sid != null && !sid.isBlank()) {
            SessionStore.getInstance().invalidate(sid);
        }

        // Clear cookie
        res.setHeader("Set-Cookie", "SID=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax; Secure");

        // Redirect to home
        res.setStatusCode(302);
        res.setStatusText("Found");
        res.setHeader("Location", "/");
        res.setHeader("Cache-Control", "no-store");
        res.setHeader("Content-Length", "0");
        res.setBody(new byte[0]);
    }

    private @org.jspecify.annotations.Nullable String readCookie(HttpRequest req, String name) {
        if (req == null || name == null) return null;
        String cookieHeader = null;
        for (var e : req.headers().entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase("Cookie")) {
                cookieHeader = e.getValue();
                break;
            }
        }
        if (cookieHeader == null || cookieHeader.isBlank()) return null;
        String[] parts = cookieHeader.split(";\\s*");
        for (String part : parts) {
            int i = part.indexOf('=');
            if (i <= 0) continue;
            String k = part.substring(0, i).trim();
            if (!k.equals(name)) continue;
            String v = part.substring(i + 1).trim();
            try {
                return URLDecoder.decode(v, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ignored) {
                return v;
            }
        }
        return null;
    }
}
