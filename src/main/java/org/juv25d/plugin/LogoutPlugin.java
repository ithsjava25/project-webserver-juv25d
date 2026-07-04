package org.juv25d.plugin;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.juv25d.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Logout endpoint for Basic Authentication environments.
 *
 * Since HTTP Basic Auth is stateless and cached by the browser, the typical
 * way to "log out" is to respond with 401 Unauthorized and a (new) realm so
 * the browser discards previously cached credentials and prompts again next time.
 *
 * This plugin returns 401 with a WWW-Authenticate header and a small HTML page
 * informing the user and redirecting back to "/" immediately (via HTTP Refresh header)
 * with meta/JS fallbacks.
 */
public class LogoutPlugin implements Plugin {

    // Realm hämtas dynamiskt för att kunna roteras vid utloggning
    private @org.jspecify.annotations.Nullable String header(org.juv25d.http.HttpRequest req, String name) {
        for (var e : req.headers().entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(name)) {
                return e.getValue();
            }
        }
        return null;
    }

    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {
        // Vi stödjer två lägen:
        // 1) Tyst ("silent") logout via bakgrundsanrop från UI: bumpa realm men skicka INTE 401/WWW-Authenticate
        //    för att undvika att webbläsaren visar en login‑popup. Returnera 204 No Content.
        // 2) Normal logout (direktbesök till /logout i adressfältet): bumpa realm och svara 401 + nytt realm
        //    samt snabb redirect till "/" via Refresh/meta/JS.

        boolean silent = false;
        // Kolla frågesträng: .../logout?silent=1
        String qs = req.queryString();
        if (qs != null && !qs.isBlank()) {
            // enkel kontroll utan parsning av separata parametrar
            String qsl = qs.toLowerCase();
            silent = qsl.contains("silent=1") || qsl.contains("silent=true");
        }
        // Tillåt även en header från klienten
        if (!silent) {
            String h = header(req, "X-Background-Logout");
            if (h != null && ("1".equals(h) || "true".equalsIgnoreCase(h))) {
                silent = true;
            }
        }

        // Rotera realm-versionen så att nästa /login inte kan återanvända tidigare Basic Auth‑uppgifter
        // från webbläsarens cache (kräver ny inmatning).
        org.juv25d.util.BootInfo.bumpRealmVersion();

        if (silent) {
            // Tyst läge: INGEN WWW-Authenticate, INGEN 401. Bara 204 och no-store.
            res.setStatusCode(204); // No Content
            res.setStatusText("No Content");
            res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            res.setHeader("Pragma", "no-cache");
            res.setHeader("Expires", "0");
            res.setHeader("Content-Length", "0");
            res.setBody(new byte[0]);
            return;
        }

        String html = """
            <!doctype html>
            <html lang=\"sv\">
            <head>
              <meta charset=\"utf-8\">
              <title>Utloggad</title>
              <meta http-equiv=\"refresh\" content=\"0;url=/\">
              <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">
              <style>
                body { font-family: system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif; padding: 2rem; }
                .card { max-width: 520px; margin: 10vh auto; padding: 1.5rem 2rem; border: 1px solid #e5e7eb; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.04); }
                h1 { margin: 0 0 0.5rem; font-size: 1.4rem; }
                p { margin: 0.5rem 0; color: #374151; }
                a { color: #2563eb; text-decoration: none; }
                a:hover { text-decoration: underline; }
              </style>
            </head>
            <body>
              <div class=\"card\">
                <h1>Du är utloggad</h1>
                <p>Du omdirigeras nu till startsidan.</p>
                <p>Om det inte händer automatiskt, <a href=\"/\">klicka här</a>.</p>
              </div>
              <script>
                // Fallback if meta/HTTP refresh inte fungerar
                (function(){
                  try {
                    // Försök rensa eventuell session-markör för denna uppstart så att UI döljer "Logga ut".
                    fetch('/auth/status', { method: 'GET', cache: 'no-store', redirect: 'manual' })
                      .then(function(res){
                        var bootId = res.headers.get('X-Boot-Id');
                        if (bootId) {
                          try { sessionStorage.removeItem('auth:' + bootId); } catch(e) { /* ignore */ }
                        }
                      })
                      .catch(function(){});
                  } catch(e) { /* ignore */ }
                  // Säkerställ snabb redirect även om meta/HTTP refresh inte triggar
                  setTimeout(function(){ window.location.replace('/'); }, 100);
                })();
              </script>
            </body>
            </html>
            """;

        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        res.setStatusCode(HttpStatus.UNAUTHORIZED.getCode());
        res.setStatusText(HttpStatus.UNAUTHORIZED.getDescription());
        // Använd nuvarande logout‑realm (matchar aktuell version efter bump)
        res.setHeader("WWW-Authenticate", "Basic realm=\"" + org.juv25d.util.BootInfo.currentLogoutRealm() + "\"");
        // Extra signal som flera webbläsare hedrar för omedelbar redirect
        res.setHeader("Refresh", "0; url=/");
        // Förhindra caching av denna 401-sida så att den inte återanvänds i back/forward-cache
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Expires", "0");
        res.setHeader("Vary", "Authorization");
        res.setHeader("Content-Type", "text/html; charset=UTF-8");
        res.setHeader("Content-Length", String.valueOf(body.length));
        res.setBody(body);
    }
}
