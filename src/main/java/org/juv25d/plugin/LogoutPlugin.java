package org.juv25d.plugin;

import org.juv25d.auth.SessionStore;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.util.CookieUtils;

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
        // Tillåt både GET och POST för att förenkla utloggning via länk eller formulär
        if (!("POST".equalsIgnoreCase(req.method()) || "GET".equalsIgnoreCase(req.method()))) {
            res.setStatusCode(405);
            res.setStatusText("Method Not Allowed");
            byte[] body = "Method Not Allowed".getBytes(StandardCharsets.UTF_8);
            res.setHeader("Content-Type", "text/plain; charset=UTF-8");
            res.setHeader("Content-Length", String.valueOf(body.length));
            res.setBody(body);
            return;
        }

        String sid = CookieUtils.readCookie(req, "SID");
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

}
