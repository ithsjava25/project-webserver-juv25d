package org.juv25d.plugin;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.http.HttpStatus;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Basic Authentication as a Plugin.
 *
 * Reads credentials from a simple Users file (one entry per line):
 *   username:password
 * Lines starting with '#' and blank lines are ignored.
 *
 * Activation rule:
 * - If the Users file is missing or contains no valid entries, the plugin is PASS-THROUGH
 *   (does not enforce authentication). This avoids breaking setups/tests without credentials configured.
 * - If the Users file exists and has at least one user, the plugin requires
 *   an Authorization: Basic header matching one of the entries.
 *
 * Configuration precedence:
 * - System property "users.file"
 * - Environment variable "USERS_FILE"
 * - Default path: "config/Users"
 */
public class BasicAuthPlugin implements Plugin {

    private volatile Map<String, String> users = Collections.emptyMap();
    private volatile boolean active = false;
    // Realm genereras dynamiskt och kan roteras per utloggning/uppstart

    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {
        ensureLoaded();

        if (!active) {
            // No Users file found or empty -> do not enforce auth
            // Expose auth state for UI logic
            res.setHeader("X-Auth-Active", "false");
            return;
        }

        // Expose that auth is active for all subsequent branches
        res.setHeader("X-Auth-Active", "true");

        // Gating: Om en ny realm-version har bumpats (t.ex. via /logout) men vi ännu
        // inte skickat en 401-utmaning för den versionen, tvinga fram en 401 nu
        // oavsett om klienten skickar Authorization preemptivt.
        if (!org.juv25d.util.BootInfo.hasCurrentRealmBeenChallenged()) {
            unauthorized(res);
            return;
        }

        String auth = header(req, "Authorization");
        if (auth == null || !auth.regionMatches(true, 0, "Basic ", 0, 6)) {
            unauthorized(res);
            return;
        }

        String encoded = auth.substring(6).trim();
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            unauthorized(res);
            return;
        }

        int idx = decoded.indexOf(':');
        if (idx <= 0) {
            unauthorized(res);
            return;
        }

        String username = decoded.substring(0, idx);
        String password = decoded.substring(idx + 1);

        String expected = users.get(username);
        if (expected == null || !Objects.equals(password, expected)) {
            unauthorized(res);
            return;
        }

        // Valid credentials -> mark as authenticated (leave status 200)
        res.setHeader("X-Authenticated", "true");
    }

    private void unauthorized(HttpResponse res) {
        res.setStatusCode(HttpStatus.UNAUTHORIZED.getCode());
        res.setStatusText(HttpStatus.UNAUTHORIZED.getDescription());
        // Använd ett dynamiskt realm som kan roteras (per logout) för att förhindra automatisk återanvändning
        // av tidigare inmatade uppgifter från webbläsarens Basic Auth‑cache.
        res.setHeader("WWW-Authenticate", "Basic realm=\"" + org.juv25d.util.BootInfo.currentRestrictedRealm() + "\"");
        // Markera att nuvarande realm-version har blivit "challenged" (401 skickad)
        org.juv25d.util.BootInfo.markCurrentRealmChallenged();
        // Expose state for clients detecting login visibility without triggering browser prompts
        res.setHeader("X-Auth-Active", "true");
        res.setHeader("X-Authenticated", "false");
        // Förhindra caching av 401-svar i mellanlager/browsers
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Expires", "0");
        // Säkerställ korrekta variant-regler när Authorization varierar
        res.setHeader("Vary", "Authorization");
        byte[] body = "Unauthorized".getBytes(StandardCharsets.UTF_8);
        res.setHeader("Content-Type", "text/plain; charset=UTF-8");
        res.setHeader("Content-Length", String.valueOf(body.length));
        res.setBody(body);
    }

    private @Nullable String header(HttpRequest req, String name) {
        for (var e : req.headers().entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(name)) {
                return e.getValue();
            }
        }
        return null;
    }

    private void ensureLoaded() {
        if (users != null && !users.isEmpty() || active) return;

        // Resolve Users-filens sökväg med stöd för både fil och katalog+filnamn.
        File f = resolveUsersFilePath();
        if (!f.exists() || !f.isFile()) {
            active = false;
            users = Collections.emptyMap();
            return;
        }

        Map<String, String> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int i = line.indexOf(':');
                if (i <= 0) continue;
                String user = line.substring(0, i).trim();
                String pass = line.substring(i + 1).trim();
                if (!user.isEmpty()) {
                    map.put(user, pass);
                }
            }
        } catch (IOException e) {
            // On load failure, deactivate to avoid locking the server
            active = false;
            users = Collections.emptyMap();
            return;
        }

        users = Collections.unmodifiableMap(map);
        active = !users.isEmpty();
    }

    /**
     * Bestämmer sökvägen till Users-filen enligt prioritet:
     *  1) System property "users.file" (explicit fil)
     *  2) Miljövariabel "USERS_FILE" (explicit fil)
     *  3) System property "users.dir" eller env "USERS_DIR" + valfritt filnamn via
     *     "users.filename"/"USERS_FILENAME" (default filnamn: "Users")
     *  4) Default: "config/Users"
     */
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
        if (dir == null || dir.isBlank()) {
            dir = System.getenv("USERS_DIR");
        }
        String fileName = System.getProperty("users.filename");
        if (fileName == null || fileName.isBlank()) {
            fileName = System.getenv("USERS_FILENAME");
        }
        if (fileName == null || fileName.isBlank()) {
            fileName = "Users"; // standardfilnamn
        }

        if (dir != null && !dir.isBlank()) {
            return new File(new File(dir.trim()), fileName);
        }

        // Default till legacy-plats: "config/Users"
        return new File("config" + File.separator + "Users");
    }
}
