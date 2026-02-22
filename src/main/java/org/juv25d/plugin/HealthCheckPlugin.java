package org.juv25d.plugin;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * HealthCheckPlugin provides a simple JSON endpoint to verify the server's status.
 * Responds to /health by default.
 */
public class HealthCheckPlugin implements Plugin {


    @Override
    public void handle(HttpRequest req, HttpResponse res) throws IOException {


        String jsonBody = """
            {
              "status": "UP"
              }
            """;

        byte[] bodyBytes = jsonBody.getBytes(StandardCharsets.UTF_8);

        res.setStatusCode(200);
        res.setHeader("Content-Type", "application/json");
        res.setHeader("Content-Length", String.valueOf(bodyBytes.length));
        res.setBody(bodyBytes);
    }
}
