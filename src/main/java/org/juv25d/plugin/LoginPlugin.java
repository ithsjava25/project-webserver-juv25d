package org.juv25d.plugin;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Login endpoint that enforces BasicAuth and, on success,
 * renders a tiny page that confirms login and redirects to "/" after ~2 seconds.
 *
 * Behavior:
 * - If Users file is missing/empty, BasicAuthPlugin is pass-through and this page displays immediately.
 * - If credentials are required and missing/invalid, BasicAuthPlugin sets 401 and this plugin stops.
 * - If credentials are valid, we return 200 with a small HTML page and meta refresh to "/".
 */
public class LoginPlugin implements Plugin {

    private final BasicAuthPlugin authPlugin = new BasicAuthPlugin();

    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {
        // First, run BasicAuth. It will set 401 on failure and leave 200 on success/pass-through.
        authPlugin.handle(req, res);
        if (res.statusCode() != 200) {
            // Auth failed -> response already prepared by auth plugin (401 + WWW-Authenticate)
            return;
        }

        String html = """
            <!doctype html>
            <html lang=\"en\">
            <head>
              <meta charset=\"utf-8\">
              <title>Logged in</title>
              <meta http-equiv=\"refresh\" content=\"2;url=/\">
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
                <h1>You are logged in</h1>
              <p>Redirecting</p>
              <p>If it doesn’t happen automatically, <a href=\"/\">click here</a>.</p>
            </div>
            <script>
              // Fallback redirect (in case meta refresh is blocked)
              setTimeout(function(){ window.location.replace('/'); }, 2100);
            </script>
          </body>
          </html>
          """;

        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        res.setStatusCode(200);
        res.setStatusText("OK");
        res.setHeader("Content-Type", "text/html; charset=UTF-8");
        res.setHeader("Content-Length", String.valueOf(body.length));
        res.setBody(body);
    }
}
