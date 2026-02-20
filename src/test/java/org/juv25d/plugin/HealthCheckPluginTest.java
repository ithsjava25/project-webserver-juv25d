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

        assertEquals("application/json", res.headers().get("Content-Type"));

        String body = new String(res.body(), StandardCharsets.UTF_8);
        assertTrue(body.contains("\"status\": \"UP\""), "Body should contain status: UP");
        assertTrue(body.contains("\"localTime\""), "Body should contain localTime");
        assertTrue(body.contains("\"utcTime\""), "Body should contain utcTime");
        assertTrue(body.contains("\"server\": \"juv25d-webserver\""), "Body should contain server info");
        assertTrue(body.contains("\"buildVersion\""), "Body should contain buildVersion");
        assertTrue(body.contains("\"gitCommit\""), "Body should contain gitCommit");
        assertTrue(body.contains("\"responseTimeMs\""), "Body should contain responseTimeMs");
        assertTrue(body.contains("\"memory\""), "Body should contain memory info");

        String contentLength = res.headers().get("Content-Length");
        assertNotNull(contentLength, "Content-Length should be set");
        assertEquals(String.valueOf(res.body().length), contentLength);
    }
}
