package org.juv25d.plugin;

import org.juv25d.auth.Session;
import org.juv25d.auth.SessionStore;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Form-baserad inloggning som skapar en serversession och sätter en säker cookie (SID).
 * Validerar användare mot samma Users-fil som tidigare BasicAuth-lösning.
 */
public class LoginPlugin implements Plugin {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {
        if ("GET".equalsIgnoreCase(req.method())) {
            String csrfToken = generateCsrfToken();
            res.setHeader("Set-Cookie", "CSRF-TOKEN=" + csrfToken + "; Path=/; HttpOnly; SameSite=Lax; Secure");
            renderLoginForm(res, null, csrfToken);
            return;
        }
        if (!"POST".equalsIgnoreCase(req.method())) {
            res.setStatusCode(405);
            res.setStatusText("Method Not Allowed");
            byte[] body = "Method Not Allowed".getBytes(StandardCharsets.UTF_8);
            res.setHeader("Content-Type", "text/plain; charset=UTF-8");
            res.setHeader("Content-Length", String.valueOf(body.length));
            res.setBody(body);
            return;
        }

        // Same-origin safeguard
        if (!isSameOrigin(req)) {
            res.setStatusCode(403);
            res.setStatusText("Forbidden");
            byte[] body = "Forbidden: Same-origin check failed".getBytes(StandardCharsets.UTF_8);
            res.setHeader("Content-Type", "text/plain; charset=UTF-8");
            res.setHeader("Content-Length", String.valueOf(body.length));
            res.setBody(body);
            return;
        }

        Map<String, String> form = parseForm(req);

        // CSRF validation
        String cookieCsrf = readCookie(req, "CSRF-TOKEN");
        String formCsrf = form.get("_csrf");
        if (cookieCsrf == null || formCsrf == null || !cookieCsrf.equals(formCsrf)) {
            res.setStatusCode(403);
            res.setStatusText("Forbidden");
            byte[] body = "Forbidden: CSRF validation failed".getBytes(StandardCharsets.UTF_8);
            res.setHeader("Content-Type", "text/plain; charset=UTF-8");
            res.setHeader("Content-Length", String.valueOf(body.length));
            res.setBody(body);
            return;
        }

        String username = form.getOrDefault("username", "").trim();
        String password = form.getOrDefault("password", "").trim();

        if (username.isEmpty() || password.isEmpty()) {
            String csrfToken = readCookie(req, "CSRF-TOKEN");
            if (csrfToken == null) {
                csrfToken = generateCsrfToken();
                res.setHeader("Set-Cookie", "CSRF-TOKEN=" + csrfToken + "; Path=/; HttpOnly; SameSite=Lax; Secure");
            }
            renderLoginForm(res, "Missing username or password", csrfToken);
            return;
        }

        Map<String, String> users = loadUsers(resolveUsersFilePath());
        String expected = users.get(username);
        if (expected == null || !Objects.equals(expected, password)) {
            String csrfToken = readCookie(req, "CSRF-TOKEN");
            if (csrfToken == null) {
                csrfToken = generateCsrfToken();
                res.setHeader("Set-Cookie", "CSRF-TOKEN=" + csrfToken + "; Path=/; HttpOnly; SameSite=Lax; Secure");
            }
            renderLoginForm(res, "Invalid credentials", csrfToken);
            return;
        }

        // Skapa session och sätt säker cookie
        Session session = SessionStore.getInstance().create(username);
        String cookie = buildSidCookie(session.getId(), SessionStore.getInstance().getIdleTimeoutSeconds());

        res.setStatusCode(302);
        res.setStatusText("Found");
        res.setHeader("Location", "/");
        res.setHeader("Set-Cookie", cookie);
        res.setHeader("Cache-Control", "no-store");
        res.setHeader("Content-Length", "0");
        res.setBody(new byte[0]);
    }

    private void renderLoginForm(HttpResponse res, @org.jspecify.annotations.Nullable String error, String csrfToken) {
        String err = (error == null) ? "" : ("<p style=\"color:#b91c1c\">" + escape(error) + "</p>");
        String html = """
            <!doctype html>
            <html lang="sv">
            <head>
              <meta charset="utf-8">
              <title>Logga in</title>
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <style>
                body { font-family: system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif; padding: 2rem; }
                .card { max-width: 520px; margin: 10vh auto; padding: 1.5rem 2rem; border: 1px solid #e5e7eb; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.04); }
                h1 { margin: 0 0 0.5rem; font-size: 1.4rem; }
                label { display:block; margin: 0.75rem 0 0.25rem; }
                input { width:100%; padding:0.5rem 0.6rem; border:1px solid #d1d5db; border-radius:8px; }
                button { margin-top:1rem; padding:0.6rem 1.2rem; border-radius:8px; border:1px solid #2563eb; background:#2563eb; color:#fff; cursor:pointer; }
              </style>
            </head>
            <body>
              <div class="card">
                <h1>Logga in</h1>
                %ERROR%
                <form method="post" action="/login">
                  <input type="hidden" name="_csrf" value="%CSRF%">
                  <label for="username">Användarnamn</label>
                  <input id="username" name="username" autocomplete="username" required>
                  <label for="password">Lösenord</label>
                  <input id="password" type="password" name="password" autocomplete="current-password" required>
                  <button type="submit">Logga in</button>
                </form>
              </div>
            </body>
            </html>
            """.replace("%ERROR%", err).replace("%CSRF%", escape(csrfToken));

        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        res.setStatusCode(200);
        res.setStatusText("OK");
        res.setHeader("Content-Type", "text/html; charset=UTF-8");
        res.setHeader("Content-Length", String.valueOf(body.length));
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Expires", "0");
        res.setBody(body);
    }

    private String buildSidCookie(String sid, long idleSeconds) {
        // Cookie attribut: HttpOnly; Secure; SameSite=Lax; Path=/
        // Max-Age sätts till idle-timeout för enkelhet (ej absolut TTL här)
        StringBuilder sb = new StringBuilder();
        sb.append("SID=").append(urlEncode(sid))
          .append("; Path=/; HttpOnly; SameSite=Lax");
        // Secure bör alltid vara på i produktion. Sätt den alltid här.
        sb.append("; Secure");
        if (idleSeconds > 0) {
            sb.append("; Max-Age=").append(idleSeconds);
        }
        return sb.toString();
    }

    private Map<String, String> parseForm(HttpRequest req) throws IOException {
        Map<String, String> map = new HashMap<>();
        if (req.body() == null || req.body().length == 0) return map;
        String contentType = header(req, "Content-Type");
        if (contentType == null || !contentType.toLowerCase().startsWith("application/x-www-form-urlencoded")) {
            return map;
        }
        String s = new String(req.body(), StandardCharsets.UTF_8);
        String[] pairs = s.split("&");
        for (String p : pairs) {
            int i = p.indexOf('=');
            String k = (i >= 0 ? p.substring(0, i) : p).trim();
            String v = (i >= 0 ? p.substring(i + 1) : "");
            if (!k.isEmpty()) {
                map.put(urlDecode(k), urlDecode(v));
            }
        }
        return map;
    }

    private String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8);
        } catch (Exception e) { return s; }
    }
    private String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) { return s; }
    }

    private @org.jspecify.annotations.Nullable String header(HttpRequest req, String name) {
        for (var e : req.headers().entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(name)) return e.getValue();
        }
        return null;
    }

    private String generateCsrfToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean isSameOrigin(HttpRequest req) {
        String origin = header(req, "Origin");
        String referer = header(req, "Referer");
        String host = header(req, "Host");

        if (origin != null) {
            // Very basic check: origin should contain host if we don't have full URL info
            return origin.contains(host != null ? host : "");
        }
        if (referer != null) {
            return referer.contains(host != null ? host : "");
        }
        // If neither is present, we might allow it depending on strictness,
        // but for login POST, at least one is usually present in browsers.
        // Let's be a bit lenient if neither is present but token is valid.
        return true;
    }

    private @org.jspecify.annotations.Nullable String readCookie(HttpRequest req, String name) {
        String cookieHeader = header(req, "Cookie");
        if (cookieHeader == null || cookieHeader.isBlank()) return null;
        String[] parts = cookieHeader.split(";\\s*");
        for (String part : parts) {
            int i = part.indexOf('=');
            if (i <= 0) continue;
            String k = part.substring(0, i).trim();
            if (!k.equals(name)) continue;
            String v = part.substring(i + 1).trim();
            return urlDecode(v);
        }
        return null;
    }

    private File resolveUsersFilePath() {
        String explicitFile = System.getProperty("users.file");
        if (explicitFile != null && !explicitFile.isBlank()) {
            return new File(explicitFile.trim());
        }
        String explicitFileEnv = System.getenv("USERS_FILE");
        if (explicitFileEnv != null && !explicitFileEnv.isBlank()) {
            return new File(explicitFileEnv.trim());
        }
        String dir = System.getProperty("users.dir");
        if (dir == null || dir.isBlank()) dir = System.getenv("USERS_DIR");
        String fileName = System.getProperty("users.filename");
        if (fileName == null || fileName.isBlank()) fileName = System.getenv("USERS_FILENAME");
        if (fileName == null || fileName.isBlank()) fileName = "Users";
        if (dir != null && !dir.isBlank()) return new File(new File(dir.trim()), fileName);
        return new File("config" + File.separator + "Users");
    }

    private Map<String, String> loadUsers(File f) throws IOException {
        Map<String, String> map = new HashMap<>();
        if (f == null || !f.exists() || !f.isFile()) return map;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int i = line.indexOf(':');
                if (i <= 0) continue;
                String user = line.substring(0, i).trim();
                String pass = line.substring(i + 1).trim();
                if (!user.isEmpty()) map.put(user, pass);
            }
        }
        return map;
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
