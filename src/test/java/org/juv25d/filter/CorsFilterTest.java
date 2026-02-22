package org.juv25d.filter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpResponse.BodyHandlers;
import static org.junit.jupiter.api.Assertions.*;

public class CorsFilterTest {

    private static final String BASE_URL = "http://localhost:3000";
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void ensureServerIsRunning() {
        // TODO: starta servern här, t.ex:
        // Server.start(3000);
        //
    }

    @Test
    void shouldAllowConfiguredOrigin_onGet() throws Exception {
        HttpResponse<String> response = client.send(
            request("GET", "/api/test")
                .header("Origin", "http://localhost:3000")
                .build(),
            BodyHandlers.ofString()
        );
        assertEquals(200, response.statusCode());
        assertEquals(
            "http://localhost:3000",
            response.headers().firstValue("Access-Control-Allow-Origin").orElse(null)
        );
        assertEquals(
            "Origin",
            response.headers().firstValue("Vary").orElse(null)
        );
    }

    @Test
    void shouldNotAddCorsHeaders_whenNoOriginHeader() throws Exception {
        HttpResponse<String> response = client.send(
            request("GET", "/api/test").build(),
            BodyHandlers.ofString()
        );
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Access-Control-Allow-Origin").isEmpty());
    }

    @Test
    void shouldHandlePreflightOptionsRequest() throws Exception {
        HttpResponse<String> response = client.send(
            request("OPTIONS", "/api/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Content-Type")
                .build(),
            BodyHandlers.ofString()
        );
        assertEquals(204, response.statusCode());
        assertEquals(
            "http://localhost:3000",
            response.headers().firstValue("Access-Control-Allow-Origin").orElse(null)
        );
        assertTrue(
            response.headers().firstValue("Access-Control-Allow-Methods")
                .orElse("")
                .contains("GET"),
            "Allow-Methods should contain GET"
        );
        assertEquals(
            "Content-Type",
            response.headers().firstValue("Access-Control-Allow-Headers").orElse(null)
        );
    }

    @Test
    void shouldNotAllowUnknownOrigin() throws Exception {
        HttpResponse<String> response = client.send(
            request("GET", "/api/test")
                .header("Origin", "http://evil.com")
                .build(),
            BodyHandlers.ofString()
        );
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Access-Control-Allow-Origin").isEmpty());
    }

    // Helper method
    private static HttpRequest.Builder request(String method, String path) {
        return HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + path))
            .method(method, HttpRequest.BodyPublishers.noBody());
    }
}
