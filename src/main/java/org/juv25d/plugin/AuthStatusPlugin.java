package org.juv25d.plugin;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Endpoint som rapporterar autentiseringsstatus UTAN att trigga Basic Auth-prompt.
 *
 * Syfte: Låta startsidan fråga om användaren är inloggad, utan att någon 401/WWW-Authenticate
 * skickas. Den här endpointen returnerar alltid 200 och sätter samma hjälpheaders som
 * BasicAuthPlugin använder: "X-Auth-Active" och "X-Authenticated".
 *
 * Svar (200, application/json):
 *   { "active": boolean, "authenticated": boolean, "user": string|null }
 */
public class AuthStatusPlugin implements Plugin {

    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {
        // Läs in Users
        File usersFile = resolveUsersFilePath();
        Map<String, String> users = loadUsers(usersFile);
        boolean active = !users.isEmpty();

        @Nullable String user = null;
        boolean authenticated = false;
        boolean challenged = org.juv25d.util.BootInfo.hasCurrentRealmBeenChallenged();

        if (active) {
            String auth = header(req, "Authorization");
            if (auth != null && auth.regionMatches(true, 0, "Basic ", 0, 6)) {
                String encoded = auth.substring(6).trim();
                try {
                    String decoded = new String(java.util.Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
                    int idx = decoded.indexOf(':');
                    if (idx > 0) {
                        String username = decoded.substring(0, idx);
                        String password = decoded.substring(idx + 1);
                        String expected = users.get(username);
                        if (expected != null && Objects.equals(expected, password)) {
                            authenticated = true;
                            user = username;
                        }
                    }
                } catch (IllegalArgumentException ignored) {
                    // lämna authenticated=false
                }
            }
        }

        // Spegla serverns 401-gating även i status-endpointen:
        // Om nuvarande realm-version ännu INTE blivit "challenged" (dvs. ingen 401 har
        // skickats efter senaste bump), rapportera authenticated=false oavsett ev.
        // Authorization-header. Detta förhindrar att UI felaktigt visar "inloggad"
        // innan webbläsaren tvingats bekräfta nya inloggningsuppgifter.
        if (active && !challenged) {
            authenticated = false;
            user = null;
        }

        // Sätt samma indikator-headers som i BasicAuthPlugin, men utan att skicka 401
        res.setHeader("X-Auth-Active", active ? "true" : "false");
        res.setHeader("X-Authenticated", authenticated ? "true" : "false");
        // Exponera ett boot-specifikt ID så att klienten kan känna igen aktuell uppstart
        // och lagra en sessionsmarkör (utan cookies) vid lyckad inloggning.
        res.setHeader("X-Boot-Id", org.juv25d.util.BootInfo.getBootId());
        // Diagnostik: exponera nuvarande realm-version (ofarligt att läcka)
        res.setHeader("X-Realm-Version", String.valueOf(org.juv25d.util.BootInfo.getRealmVersion()));

        String json = toJson(active, authenticated, user);
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        res.setStatusCode(200);
        res.setStatusText("OK");
        res.setHeader("Content-Type", "application/json; charset=UTF-8");
        res.setHeader("Content-Length", String.valueOf(body.length));
        // Förhindra att mellanlager/browsers cache:ar auth-status tvärs över användare/uppstarter
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Expires", "0");
        // Säkerställ att ev. cache skiljer på när Authorization varierar
        res.setHeader("Vary", "Authorization");
        res.setBody(body);
    }

    private String toJson(boolean active, boolean authenticated, @Nullable String user) {
        StringBuilder sb = new StringBuilder();
        sb.append('{')
          .append("\"active\":").append(active)
          .append(',')
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

    private Map<String, String> loadUsers(File f) {
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
                if (!user.isEmpty()) {
                    map.put(user, pass);
                }
            }
        } catch (IOException ignored) {
            // Vid fel, returnera tom -> inactive
        }
        return map;
    }

    private @Nullable String header(HttpRequest req, String name) {
        for (var e : req.headers().entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(name)) {
                return e.getValue();
            }
        }
        return null;
    }

    /**
     * Samma sökvägsupplösning som i BasicAuthPlugin.
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

        return new File("config" + File.separator + "Users");
    }
}
