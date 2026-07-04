package org.juv25d.plugin;

import org.juv25d.auth.Session;
import org.juv25d.auth.SessionStore;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Status-endpoint som rapporterar om användaren är autentiserad baserat på SID-cookien.
 * Returnerar alltid 200 (ingen redirect/401) så att klienten tryggt kan anropa den från startsidan.
 *
 * Svar (200, application/json; charset=UTF-8):
 *   { "authenticated": boolean, "user": string|null }
 */
public class SessionStatusPlugin implements Plugin {

    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {
        String sid = readCookie(req, "SID");
        Session session = (sid != null) ? SessionStore.getInstance().get(sid) : null;
        boolean authenticated = session != null;
        String user = null;
        if (session != null) {
            user = session.getUser();
        }

        String json = toJson(authenticated, user);
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        res.setStatusCode(200);
        res.setStatusText("OK");
        res.setHeader("Content-Type", "application/json; charset=UTF-8");
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Expires", "0");
        res.setHeader("Content-Length", String.valueOf(body.length));
        res.setBody(body);
    }

    private String toJson(boolean authenticated, @org.jspecify.annotations.Nullable String user) {
        StringBuilder sb = new StringBuilder();
        sb.append('{')
          .append("\"authenticated\":").append(authenticated)
          .append(',')
          .append("\"user\":");
        if (user == null) sb.append("null"); else sb.append('"').append(escape(user)).append('"');
        sb.append('}');
        return sb.toString();
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
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
                return URLDecoder.decode(v, StandardCharsets.UTF_8);
            } catch (Exception ignored) {
                return v;
            }
        }
        return null;
    }
}
