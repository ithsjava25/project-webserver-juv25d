package org.juv25d.filter;

import org.juv25d.http.HttpRequest;
import org.juv25d.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CorsFilterTest {

    private final CorsFilter filter = new CorsFilter();

    @Test
    void shouldAllowConfiguredOrigin_onGet() throws Exception {
        HttpRequest req = request("GET", "/api/test", Map.of("Origin", "http://localhost:3000"));
        HttpResponse res = response();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertEquals("http://localhost:3000", res.getHeader("Access-Control-Allow-Origin"));
        assertEquals("Origin", res.getHeader("Vary"));
    }

    @Test
    void shouldNotAddCorsHeaders_whenNoOriginHeader() throws Exception {
        HttpRequest req = request("GET", "/api/test", Map.of());
        HttpResponse res = response();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertNull(res.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    void shouldHandlePreflightOptionsRequest() throws Exception {
        HttpRequest req = request(
            "OPTIONS",
            "/api/test",
            Map.of(
                "Origin", "http://localhost:3000",
                "Access-Control-Request-Method", "GET",
                "Access-Control-Request-Headers", "Content-Type"
            )
        );
        HttpResponse res = response();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        verify(chain, never()).doFilter(any(), any());
        assertEquals(204, res.statusCode());
        assertEquals("No Content", res.statusText());
        assertEquals("http://localhost:3000", res.getHeader("Access-Control-Allow-Origin"));
        assertTrue(res.getHeader("Access-Control-Allow-Methods").contains("GET"));
        assertEquals("Content-Type", res.getHeader("Access-Control-Allow-Headers"));
        assertArrayEquals(new byte[0], res.body());
    }

    @Test
    void shouldNotAllowUnknownOrigin() throws Exception {
        HttpRequest req = request("GET", "/api/test", Map.of("Origin", "http://evil.com"));
        HttpResponse res = response();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertNull(res.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    void shouldFallbackToDefaultAllowHeaders_onPreflightWithoutRequestHeaders() throws Exception {
        HttpRequest req = request(
            "OPTIONS",
            "/api/test",
            Map.of(
                "Origin", "http://localhost:3000",
                "Access-Control-Request-Method", "GET"
            )
        );
        HttpResponse res = response();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        verify(chain, never()).doFilter(any(), any());
        assertEquals(204, res.statusCode());
        assertEquals("Content-Type", res.getHeader("Access-Control-Allow-Headers"));
    }

    private static HttpRequest request(String method, String path, Map<String, String> headers) {
        return new HttpRequest(
            method,
            path,
            "",
            "HTTP/1.1",
            new HashMap<>(headers),
            new byte[0],
            "127.0.0.1"
        );
    }

    private static HttpResponse response() {
        return new HttpResponse(200, "OK", new HashMap<>(), new byte[0]);
    }
}
