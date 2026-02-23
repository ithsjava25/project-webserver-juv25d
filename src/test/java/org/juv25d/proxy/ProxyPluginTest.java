package org.juv25d.proxy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProxyPluginTest {

    private ProxyRoute proxyRoute;
    private ProxyPlugin proxyPlugin;

    @BeforeEach
    void setUp() {
        this.proxyRoute = new ProxyRoute("/api", "https://invalid-upstream-domain-ex-juv25d.info");
        this.proxyPlugin = new ProxyPlugin(proxyRoute);
    }

    @DisplayName("Should handle the request to an invalid upstream and return 502")
    @Test
    void handleInvalidDomain() throws IOException {
        HttpRequest req = new HttpRequest(
            "GET",
            "/api/users",
            null,
            "HTTP/1.1",
            Map.of("Content-Type", "application/json"),
            new byte[0],
            "127.0.0.1"
        );
        HttpResponse res = new HttpResponse();

        proxyPlugin.handle(req, res);

        // returns 502 Bad Gateway when connection fails
        assertEquals(502, res.statusCode());
    }

    @DisplayName("proxies the request to valid upstream target server but non existing resource path and relay 404")
    @Test
    void upstreamResourceNotFound() throws IOException {
        this.proxyRoute = new ProxyRoute("/api", "https://jsonplaceholder.typicode.com");
        this.proxyPlugin = new ProxyPlugin(proxyRoute);

        HttpRequest req = new HttpRequest(
            "GET",
            "/api/test-resource",
            null,
            "HTTP/1.1",
            Map.of("Content-Type", "application/json"),
            new byte[0],
            "127.0.0.1"
        );
        HttpResponse res = new HttpResponse();

        proxyPlugin.handle(req, res);

        assertEquals(404, res.statusCode());
    }

    @DisplayName("returns 200 with response body")
    @Test
    void successfulResponse() throws IOException {
        this.proxyRoute = new ProxyRoute("/api", "https://jsonplaceholder.typicode.com");
        this.proxyPlugin = new ProxyPlugin(proxyRoute);

        HttpRequest req = new HttpRequest(
            "GET",
            "/api/posts",
            null,
            "HTTP/1.1",
            Map.of("Content-Type", "application/json"),
            new byte[0],
            "127.0.0.1"
        );

        HttpResponse res = new HttpResponse();

        proxyPlugin.handle(req, res);

        assertEquals(200, res.statusCode());
        assertNotNull(res.body());
        assertTrue(res.body().length > 0);
    }
}

