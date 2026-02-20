package org.juv25d.plugin;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * HealthCheckPlugin provides a simple JSON endpoint to verify the server's status.
 * Responds to /health by default.
 */
public class HealthCheckPlugin implements Plugin {
    private static final String SERVER_NAME = "juv25d-webserver/1.0";

    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {
        String timestamp = Instant.now().toString();
        String jsonBody = String.format(
            "{\"status\": \"UP\", \"timestamp\": \"%s\", \"server\": \"%s\"}",
            timestamp, SERVER_NAME
        );

        res.setStatusCode(200);
        res.setStatusText("OK");
        byte[] bodyBytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        res.setHeader("Content-Type", "application/json");
        res.setHeader("Content-Length", String.valueOf(bodyBytes.length));
        res.setBody(bodyBytes);
    }
}
