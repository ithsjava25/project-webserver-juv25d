package org.juv25d.plugin;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckPluginTest {

    @Test
    void sets200StatusAndJsonBody() throws IOException {
        HealthCheckPlugin plugin = new HealthCheckPlugin();
        HttpRequest req = new HttpRequest("GET", "/health", null, "HTTP/1.1", Map.of(), new byte[0], "HEALTH");
        HttpResponse res = new HttpResponse();

        plugin.handle(req, res);

        assertEquals(200, res.statusCode());
        assertEquals("OK", res.statusText());
        assertEquals("application/json", res.headers().get("Content-Type"));

        String body = new String(res.body(), StandardCharsets.UTF_8);
        assertTrue(body.contains("\"status\": \"UP\""), "Body should contain status: UP");
        assertTrue(body.contains("\"timestamp\""), "Body should contain a timestamp");
        assertTrue(body.contains("\"server\": \"juv25d-webserver/1.0\""), "Body should contain server info");

        String contentLength = res.headers().get("Content-Length");
        assertNotNull(contentLength, "Content-Length should be set");
        assertEquals(String.valueOf(res.body().length), contentLength);
    }
}
