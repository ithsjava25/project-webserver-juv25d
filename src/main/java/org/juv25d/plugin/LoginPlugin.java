package org.juv25d.plugin;

import org.juv25d.auth.Session;
import org.juv25d.auth.SessionStore;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.util.CookieUtils;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Form baserad inloggning som skapar en serversession och sätter en säker cookie (SID).
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

        // CSRF validation inaktiverad på begäran för att undvika lokala 403-problem.
        // Vi läser fortfarande värdena för framtida bruk, men nekar inte på mismatch.
        String cookieCsrf = CookieUtils.readCookie(req, "CSRF-TOKEN");
        String formCsrf = form.get("_csrf");

        String username = form.getOrDefault("username", "").trim();
        // Preserve the exact password as submitted (no trimming), but keep empty default
        String password = form.getOrDefault("password", "");

        if (username.isEmpty() || password.isEmpty()) {
            String csrfToken = CookieUtils.readCookie(req, "CSRF-TOKEN");
            if (csrfToken == null) {
                csrfToken = generateCsrfToken();
                res.setHeader("Set-Cookie", "CSRF-TOKEN=" + csrfToken + "; Path=/; HttpOnly; SameSite=Lax; Secure");
            }
            renderLoginForm(res, "Missing username or password", csrfToken);
            return;
        }

        Map<String, String> users = loadUsers(resolveUsersFilePath());
        String expectedHash = users.get(username);
        // Om exakt nyckel inte finns, försök med case-insensitive match för användarnamn
        String canonicalUser = username;
        if (expectedHash == null && !users.isEmpty()) {
            for (Map.Entry<String, String> e : users.entrySet()) {
                if (e.getKey().equalsIgnoreCase(username)) {
                    expectedHash = e.getValue();
                    canonicalUser = e.getKey(); // behåll originalcasing från Users-filen för sessionen
                    break;
                }
            }
        }
        if (expectedHash == null || !verifyPassword(password, expectedHash)) {
            String csrfToken = CookieUtils.readCookie(req, "CSRF-TOKEN");
            if (csrfToken == null) {
                csrfToken = generateCsrfToken();
                res.setHeader("Set-Cookie", "CSRF-TOKEN=" + csrfToken + "; Path=/; HttpOnly; SameSite=Lax; Secure");
            }
            renderLoginForm(res, "Invalid credentials", csrfToken);
            return;
        }

        // Skapa session och sätt säker cookie
        Session session = SessionStore.getInstance().create(canonicalUser);
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
        // Same-origin kontroll inaktiverad på begäran.
        return true;
    }

    private boolean sameAuthority(String hostHeader, String uriString) {
        java.net.URI uri;
        try {
            uri = new java.net.URI(uriString);
        } catch (java.net.URISyntaxException e) {
            // Invalid URI: fail closed
            return false;
        }

        String scheme = uri.getScheme();
        String uriHost = uri.getHost();
        int uriPort = uri.getPort();

        if (scheme == null || uriHost == null) {
            return false; // Must be an absolute URI with host
        }

        int uriNormPort = normalizePort(scheme, uriPort);

        HostPort req = parseHostHeader(hostHeader);
        if (req == null) return false;

        // If request Host header omitted port, assume scheme's default from the URI being checked
        int reqNormPort = (req.port >= 0) ? req.port : normalizePort(scheme, -1);

        return uriHost.equalsIgnoreCase(req.host) && uriNormPort == reqNormPort;
    }

    private int normalizePort(String scheme, int port) {
        if (port >= 0) return port;
        if (scheme.equalsIgnoreCase("http")) return 80;
        if (scheme.equalsIgnoreCase("https")) return 443;
        return -1; // Unknown scheme, can't infer
    }

    private static final class HostPort {
        final String host;
        final int port; // -1 if not specified
        HostPort(String host, int port) { this.host = host; this.port = port; }
    }

    private @org.jspecify.annotations.Nullable HostPort parseHostHeader(String hostHeader) {
        String h = hostHeader.trim();
        if (h.isEmpty()) return null;

        // Handle IPv6 [::1]:port per RFC 7230
        if (h.startsWith("[") ) {
            int end = h.indexOf(']');
            if (end <= 0) return null;
            String host = h.substring(1, end);
            int port = -1;
            if (end + 1 < h.length() && h.charAt(end + 1) == ':') {
                String p = h.substring(end + 2);
                try { port = Integer.parseInt(p); } catch (NumberFormatException e) { return null; }
            }
            return new HostPort(host, port);
        }

        int idx = h.lastIndexOf(':');
        if (idx > 0 && h.indexOf(':') == idx) {
            // Single colon -> host:port
            String host = h.substring(0, idx);
            String p = h.substring(idx + 1);
            try {
                int port = Integer.parseInt(p);
                return new HostPort(host, port);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        // No port specified (or multiple colons like IPv6 without brackets, treat as invalid)
        if (h.indexOf(':') >= 0) return null; // likely malformed
        return new HostPort(h, -1);
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

    private boolean verifyPassword(String password, String hashed) {
        if (hashed == null) return false;
        // Backwards compatibility: allow plain-text password entries (user:password)
        if (!hashed.startsWith("pbkdf2:")) {
            byte[] a = password.getBytes(StandardCharsets.UTF_8);
            byte[] b = hashed.getBytes(StandardCharsets.UTF_8);
            return MessageDigest.isEqual(a, b);
        }
        try {
            String[] parts = hashed.split(":");
            final int iterations;
            final byte[] salt;
            final byte[] hash;

            // Support legacy format: pbkdf2:<iterations>:<saltB64>:<hashB64>
            // and versioned format:   pbkdf2:v2:<iterations>:<saltB64>:<hashB64>
            if (parts.length == 4) {
                iterations = Integer.parseInt(parts[1]);
                salt = Base64.getDecoder().decode(parts[2]);
                hash = Base64.getDecoder().decode(parts[3]);
            } else if (parts.length == 5 && "v2".equalsIgnoreCase(parts[1])) {
                iterations = Integer.parseInt(parts[2]);
                salt = Base64.getDecoder().decode(parts[3]);
                hash = Base64.getDecoder().decode(parts[4]);
            } else {
                return false;
            }

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, hash.length * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] testHash = factory.generateSecret(spec).getEncoded();

            return MessageDigest.isEqual(hash, testHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Utility method to generate a password hash.
     * Can be used by administrators to generate hashes for the Users file.
     */
    public String hashPassword(String password) {
        try {
            // Increase iteration count substantially for stronger security.
            // New versioned format: pbkdf2:v2:<iterations>:<saltB64>:<hashB64>
            int iterations = 210_000;
            byte[] salt = new byte[16];
            secureRandom.nextBytes(salt);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return "pbkdf2:v2:" + iterations + ":" + Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
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
